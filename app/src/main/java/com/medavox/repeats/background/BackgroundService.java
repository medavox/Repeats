package com.medavox.repeats.background;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import android.util.Log;
import com.medavox.repeats.application.Application;
import com.medavox.repeats.backend.Backend;
import com.medavox.repeats.backend.BackendHelper;
import com.medavox.repeats.datamodels.CompletedDose;
import com.medavox.repeats.datamodels.IntendedDose;
import com.medavox.repeats.utility.DateTime;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**@author Adam Howard
@date 25/07/2016
 */

/**Handles long-running tasks which don't require user input, such as checking for due doses,
 * re-sending cached data from offline doses*/
public class BackgroundService extends Service {

    /*services are by design singletons; onCreate() is only called once when the service is created,
    * but onStartCommand() will be called whenever startService() is called,
    * and the running service will be reused.
    * There can only be 0 or 1 instance(s) of a service running*/

    /**Tag for use in Log calls*/
    private static final String TAG = BackgroundService.class.toString();

    /**Determines if the service is already running.
    Prevents launching the service twice.*/
    private static long ALARM_WINDOW_LENGTH = 5 * 60*1000;//5 minute window for OS to run alarm
    public static final String PACKAGE_PREFIX = "com.elucid.medi ";
    public static final String DOSE_QUANTITY_TAG = PACKAGE_PREFIX+"dose quantity";
    public static final String DOSE_ID_TAG = PACKAGE_PREFIX+"dose ID";
    public static final String DOSE_START_TIME_TAG = PACKAGE_PREFIX+"dose start time";
    public static final String DOSE_END_TIME_TAG = PACKAGE_PREFIX+"dose end time";
    public static final String DOSE_DUE_TIME_TAG = PACKAGE_PREFIX+"dose due time";
    public static final String DOSE_ALARM_TIME_TAG = PACKAGE_PREFIX+"dose time (ms since epoch)";

    protected static Timer missedTimer = new Timer();

    /**a map of doseID integers to PendingIntents (for cancelling alarms with AlarmManager.cancel(PendingIntent)).
     * The positive indexes are for due alarms, negative for missed alarms*/
    private static Map<Integer, PendingIntent> doseAlerts = new ConcurrentHashMap<Integer, PendingIntent>();
    /**A map of doseID integers to TimerTasks, for cancelling the TimerTask which fires a DOSE_MISSED event.*/
    private static final Map<Integer, TimerTask> missedDoseTasks = new ConcurrentHashMap<Integer, TimerTask>();

    public BackgroundService() {
        super();
    }

    @Override
    public void onCreate() {
        /*PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);*/
        super.onCreate();

        switch(Application.getBuildMode()) {
            case DEBUG:
            case DEMO:
                ALARM_WINDOW_LENGTH = 5000;//set window length to 5 seconds during a demo
                break;
        }
        //sharedPreferences = this.getSharedPreferences(this.getString(R.string.SHARED_PREFERENCES_FILENAME), Context.MODE_PRIVATE);
        try {//this often causes EventBusExceptions after reinstalling the app during dev
            EventBus.getDefault().register(this);
        }//possibly due to multiple instances of BackgroundService running concurrently
        catch(EventBusException ebe) {//so it isn't worth worrying about a lot
            Log.w(TAG, ebe);
        }


        //createNextDueAlarmPair();
        //if there's a next dose, set reminders for it
        Backend helper = BackendHelper.getInstance(this);
        IntendedDose iDose = helper.getNextDueDose();
        if(iDose != null) {
            Log.i(TAG, "setting reminders for "+iDose);
            setReminders(iDose);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // If we get killed after returning here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding (yet?), so return null
        return null;
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        EventBus.getDefault().unregister(this);
    }



    //-----------------------------------eventbus subscribe methods


    //--------------------static methods for alarms/reminder management

    /**Sets up reminders for the given IntendedDose.
     * Creates a TimerTask (which fires a DoseEvent.DOSE_MISSED as soon as the end time passes);
     * creates an alarm to fire when the dose is due (which fires a notification);
     * creates a second alarm which fires when the dose is missed, to notify the user.
     * Finally, adds these to private data structures, and makes the alarms cancelable by providing the DoseID*/
    public static void setReminders(IntendedDose iDose) {
        //missedTimer = new Timer();
        int doseID = iDose.getDoseID();
        //only create and add a new missed TimerTask, if there isn't one already
        if(!missedDoseTasks.containsKey(doseID)) {
            //set a TimerTask to let us know when(/if) the dose is missed
            final IntendedDose finalDose = iDose;
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    //EventBus.getDefault().post(new DoseEvent(this, DoseEvent.DoseEventType.DOSE_MISSED, finalDose));
                    missedDoseTasks.remove(finalDose.getDoseID());
                }
            };
            missedTimer.schedule(task, new Date(iDose.getTimeEnd()));
            //add this task to the list, to keep a reference for canceling it
            missedDoseTasks.put(doseID, task);
        }
        //only create and add the alarms (which fire the intents when the time comes) if they haven't been already
        if(!doseAlerts.containsKey(doseID)) {

            //add the alarms too (this is done inside createAlarm())
            createNextDueAlarmPair(iDose);
        }
    }

    /**Sets 2 alarms for the time the next dose is due, and the time it is considered missed by.
     * Equivalent to createNextDueAlarmPair(null).
     * @return true if the alarms were set, false otherwise.
     * */
    public static boolean createNextDueAlarmPair() {
        return createNextDueAlarmPair(null);
    }

    /**Sets 2 alarms: one for the time the next dose is due (which fires a notification),
     * and one for the time it is considered missed by (which also fires a notification).
     * @return true if the alarms were set, false otherwise*/
    public static boolean createNextDueAlarmPair(IntendedDose passedDose) {
        IntendedDose iDose = (passedDose == null ? BackendHelper.getInstance(Application.getContext()).getNextDueDose() : passedDose);
        Calendar cal = Calendar.getInstance();
        if (Application.getBuildMode() == Application.BuildMode.DEBUG) {
            //actual alarm time is jittered by the minute's seconds when System.currentTimeMillis() is called
            cal.setTimeInMillis(System.currentTimeMillis());//fill in year, date, and seconds from current values
            //(using the current seconds adds some jitter to the alarm firing time)

            //create dose due alarm -- in debug mode, the alarm is set for right now
            createAlarm(Application.getContext(), DoseDueAlertService.class, cal.getTimeInMillis(), iDose);

            //in debug, set missed alarm to be a minute after due alarm
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 1);

            //create dose missed alarm
            createAlarm(Application.getContext(), DoseMissedAlertService.class, cal.getTimeInMillis(), iDose);
            return true;
        }
        else if(iDose != null) {//and also BuildMode != DEBUG
            if (Application.getBuildMode() == Application.BuildMode.DEMO && iDose == null) {
                //make it work no matter what, for demos
                iDose = IntendedDose.createDemoDoseDueIn(15000);//due in 15 seconds, to show the state change
            }
            //create dose due alarm
            cal.setTimeInMillis(iDose.getTimeStart());
            createAlarm(Application.getContext(), DoseDueAlertService.class, cal.getTimeInMillis(), iDose);

            //create dose missed alarm
            cal.setTimeInMillis(iDose.getTimeEnd());
            createAlarm(Application.getContext(), DoseMissedAlertService.class, cal.getTimeInMillis(), iDose);
        }
        return iDose != null;
    }

    public static void createAlarm(Context context, Class<?> cls, long alarmTime, IntendedDose iDose) {
        Log.i(TAG, "creating alarm for "+cls.getSimpleName()+" at "+ DateTime.getNiceFormat(alarmTime)+", with doseID: "+iDose.getDoseID());
        //dumpAllStackTraces();
        Intent intent = new Intent(context, cls)
                .putExtra(DOSE_QUANTITY_TAG,    iDose.getQuantity())
                .putExtra(DOSE_ID_TAG,          iDose.getDoseID())
                .putExtra(DOSE_END_TIME_TAG,    iDose.getTimeEnd())
                .putExtra(DOSE_DUE_TIME_TAG,    iDose.getTimeDue())
                .putExtra(DOSE_START_TIME_TAG,  iDose.getTimeStart())
                .putExtra(DOSE_ALARM_TIME_TAG,  alarmTime);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);
        int doseID = iDose.getDoseID();
        if(cls == DoseDueAlertService.class) {
            doseAlerts.put(doseID, pendingIntent);
        }
        else if(cls == DoseMissedAlertService.class) {
            doseAlerts.put(0-doseID, pendingIntent);
        }

        //set alarm to go off at the specified time
        //the device will wake up for this
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setWindow(AlarmManager.RTC_WAKEUP, alarmTime, ALARM_WINDOW_LENGTH, pendingIntent);
    }


//todo: de-duplicate code in these methods
    public static void cancelDueAlarm(int doseID) {
        AlarmManager alarmMgr = (AlarmManager)Application.getContext().getSystemService(Context.ALARM_SERVICE);
        NotificationManager noteMgr = (NotificationManager)Application.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Log.i(TAG, "cancelling due alarm for dose ID: "+doseID);

        if(doseAlerts.containsKey(doseID)) {
            cancelNotificationAlarm(doseID);
        }
    }

    /**Cancel the TimerTask and the 2 alarms (and their corresponding notifications, if they have been created already)
     * associated with this DoseID.*/
    public static void cancelRemindersWithDoseID(int doseID) {
        Log.i(TAG, "cancelling reminders for dose ID: "+doseID);

        //cancel the TimerTask
        if(missedDoseTasks.containsKey(doseID)) {
            cancelTimerTask(doseID);
        }

        //cancel the alarm (and its notification) that the dose is due
        if(doseAlerts.containsKey(doseID)) {
            Log.i(TAG, "cancelling missed PendingIntent");
            cancelNotificationAlarm(doseID);
        }

        //cancel the alarm (and its notification) that the dose has been missed
        if(doseAlerts.containsKey(0-doseID)) {
            Log.i(TAG, "cancelling missed PendingIntent");
            cancelNotificationAlarm(0-doseID);
        }
    }


    /**Probably should be debug only.
     * Cancels all TimerTasks, alarms and notifications referred to in the data structures.*/
    public static void cancelAllReminders() {
        Log.i(TAG, "cancelling all reminders...");
        for(Integer doseID : missedDoseTasks.keySet()) {
            cancelTimerTask(doseID);
        }

        for(Integer doseID : doseAlerts.keySet()) {
            String type = (doseID < 0 ? "missed " : "due ");
            Log.i(TAG, "cancelling "+type+" PendingIntent");
            cancelNotificationAlarm(doseID);
        }
    }

    private static void cancelNotificationAlarm(int doseID) {
        //get the PendingIntent for the due Dose alert
        PendingIntent pendingIntent = doseAlerts.get(doseID);
        if(pendingIntent != null) {
            //cancel the alarm (which fires the notification when it runs)
            NotificationManager noteMgr = (NotificationManager)Application.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            AlarmManager alarmMgr = (AlarmManager) Application.getContext().getSystemService(Context.ALARM_SERVICE);
            alarmMgr.cancel(pendingIntent);
            //also, cancel the actual notification (if it has been created already)
            noteMgr.cancel(doseID);
            //remove it from the collection
            doseAlerts.remove(doseID);
        }
        else {
            Log.w(TAG, "PendingIntent not found with doseID: "+doseID);
        }
    }

    private static void cancelTimerTask(int doseID) {
        //get, cancel and remove the TimerTask
        TimerTask tt = missedDoseTasks.get(doseID);
        if(tt != null) {
            Log.i(TAG, "cancelling TimerTask " + tt);
            tt.cancel();
            missedDoseTasks.remove(doseID);
        }
        else{
            Log.w(TAG, "TimerTask not found with doseID: "+doseID);
        }
    }
    private static void dumpAllStackTraces() {
        for (Map.Entry <Thread, StackTraceElement []> entry : Thread.getAllStackTraces().entrySet ()) {
            System.out.println (entry.getKey ().getName () + ":");
            for (StackTraceElement element: entry.getValue ()) {
                System.out.println("\t" + element);
            }
        }
    }
}
