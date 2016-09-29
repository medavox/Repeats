package com.medavox.repeats.background;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.medavox.repeats.R;
import com.medavox.repeats.ui.DelegatorActivity;

/**@author Adam Howard
@date 15/08/2016
 * Common functionality for Services which build Android Notifications*/
public abstract class AlertService extends IntentService {
    public static final int NOTIFICATION_ID_OFFSET = 183;
    public static final String STARTED_FROM_TAG = "Started from notification with ID";

    public AlertService(String name) {
        super(name);
    }

    /**Creates a Notification, filling in all values common to all our notifications*/
    public NotificationCompat.Builder getDefaultNotificationBuilder(String title, String text, int id) {
        NotificationCompat.Builder noteBuilder = new NotificationCompat.Builder(this);

        //build intent to return to video, on tapping notification
        Intent contentIntent = new Intent(getApplicationContext(),
                DelegatorActivity.class).putExtra(STARTED_FROM_TAG, id);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        noteBuilder
                .setSmallIcon(R.drawable.alarm)
                .setContentTitle(title)
                .setContentText(text)
                //.setSound(soundUri); //todo: find a sound
                //.setDeleteIntent()//todo
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                        0, contentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
        ;

        if (android.os.Build.VERSION.SDK_INT >= 16)
            noteBuilder.setPriority(Notification.PRIORITY_LOW);

        return noteBuilder;
        //if (android.os.Build.VERSION.SDK_INT < 21) {

        //doesn't fit with Notification.MediaStyle
        //.setProgress(vidLength, 0, false)
        //.setLargeIcon(videoThumbnail)
        //.addAction(playButton);
        //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        //.setLargeIcon(cover)



        //noteBuilder.setShowCancelButton(true);
        //.setMediaSession(mMediaSession.getSessionToken())

        /*} else {
            RemoteViews view =
                    new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.player_notification);
            view.setImageViewBitmap(R.id.backgroundCover, videoThumbnail);
            view.setTextViewText(R.id.backgroundSongName, title);
            view.setTextViewText(R.id.backgroundArtist, channelName);
            view.setOnClickPendingIntent(R.id.backgroundStop, stopPI);
            view.setOnClickPendingIntent(R.id.backgroundPlayPause, playPI);

            noteBuilder.setCategory(Notification.CATEGORY_REMINDER);//could also be ALARM

            //Make notification appear on lockscreen
            noteBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

            note = noteBuilder.build();
            note.contentView = view;

            note.bigContentView = expandedView;
        }*/

    }
}
