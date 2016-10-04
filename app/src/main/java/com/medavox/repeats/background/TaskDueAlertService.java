package com.medavox.repeats.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.medavox.repeats.R;

/**@author Adam Howard
@date 15/08/2016
Fired by an alarm, when a new dose becomes due.*/
public class TaskDueAlertService extends AlertService {
    public TaskDueAlertService() {
        super("TaskDueAlertService");
    }

    private static final String TAG = TaskDueAlertService.class.toString();

    @Override
    public void onHandleIntent(Intent intent) {
        Log.i(TAG, "Dose Due Alert Service started");
        Resources res = getApplicationContext().getResources();
        int doseID = intent
                .getIntExtra(
                BackgroundService
                        .DOSE_ID_TAG, -1);
        int noteID = doseID;
        NotificationCompat.Builder builder = getDefaultNotificationBuilder(res.getString(R.string.dose_due_notification_title), res.getString(R.string.dose_due_notification_text), noteID);
        /*Intent swipeIgnoreIntent = new Intent(getApplicationContext(),
                ReminderIgnoredService.class);
        builder.setDeleteIntent(PendingIntent.getService(getApplicationContext(), 0, swipeIgnoreIntent,
                PendingIntent.FLAG_ONE_SHOT ));*/

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        Notification note = builder.build();
        long doseTime = intent.getLongExtra(BackgroundService.DOSE_ALARM_TIME_TAG, -1);
        int doseQuant = intent.getIntExtra(BackgroundService.DOSE_QUANTITY_TAG, -1);

        if(doseID == -1
        || doseQuant == -1
        || doseTime == -1) {
            Log.e(TAG, "dose info was not set!\ndose ID:"+doseID+"\ndoseQuant:"+doseQuant+"\ndoseTime:"+doseTime);
        }
        else {
            NotificationManager noteMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            noteMgr.notify(noteID, note);
            /*NetworkController.getInstance().postMonitor(Monitor.createMonitor(this,      //context
                                                doseID,                                  //dose ID
                                                PlatformCodes.REMINDER_STARTED));        //action*/
        }
    }
}
