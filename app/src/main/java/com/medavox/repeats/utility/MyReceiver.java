package com.medavox.repeats.utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.medavox.repeats.application.Application;
import com.medavox.repeats.background.BackgroundService;

public class MyReceiver extends BroadcastReceiver
{
    private PendingIntent currentSetAlarm = null;
      
    @Override
    public void onReceive(Context context, Intent intent)
    {
     /*   WakeLocker.acquire(context);
        Log.d("JAMES_TEST", "APP in onReceive. Acquiring wakelock. Starting Dose alert");
    	Intent i = new Intent();
        i.setClassName("com.elucid.medi", "com.elucid.medi.activities.DoseAlert");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);*/
        switch(intent.getAction()) {
            case "android.intent.action.BOOT_COMPLETED":
                //re-add alarms for pending doses after device reboot
                BackgroundService.createNextDueAlarmPair();
                break;
            case "android.net.conn.CONNECTIVITY_CHANGE":
                ConnectivityManager cm =
                        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                boolean isWiFi = activeNetwork != null &&
                        activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

                if(isConnected && isWiFi /*&& OfflineCacheService.hasCachedRequests() */&& currentSetAlarm == null) {
                    /*When internet becomes available:
                * if we have any cached network requests
                * set an alarm to retry them in ~5 minutes (jittered)*/
                    //currentSetAlarm = OfflineCacheService.scheduleCacheRetry();
                }
                else if(currentSetAlarm != null) {
                    //cancel the currently set alarm (if there is one), as we no longer have WiFi
                    ((AlarmManager) Application.getContext().getSystemService(Context.ALARM_SERVICE)).cancel(currentSetAlarm);
                }

                break;
        }
    }
}