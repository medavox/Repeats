package com.medavox.repeats.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.medavox.repeats.R;
import com.medavox.repeats.utility.DateTime;

/**
 * @author Adam Howard
 * Service which is fired from an Alarm, at the end of a due dose time.
 * Performs the following tasks:
 * <ul>
 *     <li>sends a message to the platform about the missed dose</li>
 *     <li>cancels the previous doseDue notification for this dose ID, if it exists</li>
 *     <li>notifies the user that the dose was missed</li>
 *     <li>creates an alarm for the next due dose, if it hasn't been already</li>
 * </ul>
 * */
public class TaskMissedAlertService extends AlertService {

    private static final String TAG = TaskMissedAlertService.class.toString();

    public TaskMissedAlertService() {
        super("TaskMissedAlertService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.i(TAG, "Dose Missed Alert Service started");
        Resources res = getApplicationContext().getResources();
        String title = res.getString(R.string.taskMissedNotificationTitle);
        String text = res.getString(R.string.taskMissedNotificationText);

        int missedDoseID = intent.getIntExtra(BackgroundService.DOSE_ID_TAG, -1);
        long missedDoseEndTime = intent.getLongExtra(BackgroundService.DOSE_END_TIME_TAG, -1);
        text += DateTime.getNiceFormat(missedDoseEndTime);
        int newNoteID = 0-missedDoseID;//use negative notification IDs to avoid clashing with notification IDs for due Dose Alerts
        NotificationCompat.Builder builder = getDefaultNotificationBuilder(title, text, newNoteID);


        Notification note = builder.build();

        NotificationManager noteMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //cancel the previous doseDue notification for this dose ID, if it exists
        noteMgr.cancel(missedDoseID);

        //FIXME:THIS is the problem. We cancel ourselves, just after we're added to doseAlerts
        //cancel any other alarms
        //BackgroundService.cancelRemindersWithDoseID(missedDoseID);
        BackgroundService.cancelDueAlarm(missedDoseID);

        //notify the user that the dose was missed
        noteMgr.notify(newNoteID, note);

        //firing a DOSE_MISSED event is now handled by a TimerTask

        //set next pair of alarms, for the next due dose
        BackgroundService.createNextDueAlarmPair();
    }
}
