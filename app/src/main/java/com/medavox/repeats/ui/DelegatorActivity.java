package com.medavox.repeats.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.medavox.repeats.R;
import com.medavox.repeats.application.Application;
import com.medavox.repeats.background.AlertService;
import com.medavox.repeats.background.BackgroundService;
import com.medavox.repeats.background.PreferencesChangeListener;
import com.medavox.repeats.events.EventLogger;

/**
 * @author Adam Howard
@date 17/08/16
 */
/**The initial app entrypoint. Decides which interface to run based on SharedPreferences*/
public class DelegatorActivity extends AppCompatActivity {

    private static final String TAB_ACTIVITY = "com.elucid.medi.ui.TabActivity";//constants must be set at compile-time, so can't use getClass()
    private static final String SINGLE_PAGE_ACTIVITY  = "com.elucid.medi.ui.SinglePageActivity";
    private static String TAG = "DelegatorActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences.OnSharedPreferenceChangeListener prefsListener = PreferencesChangeListener.getInstance(this);
        Application.setPrefsListener(prefsListener);//create strong reference to prevent listener being garbage collected
        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_prefs_tag), MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(prefsListener);

        //start our Event Logger. Disable this to lessen event output
        EventLogger logger = new EventLogger();

        Intent intent = getIntent();
        if(intent.hasExtra(AlertService.STARTED_FROM_TAG)) { //have we been started by a notification being tapped?
            //if so, cancel that notification, as it has done its job
            NotificationManager noteMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            noteMgr.cancel(intent.getIntExtra(AlertService.STARTED_FROM_TAG, 0));
        }


        if(Application.getBuildMode() == Application.BuildMode.DEV) {
            Log.i(TAG, "DEVMODE: deleting SharedPreferences values...");
            //if we're if dev mode, delete the JWT on every app start, to test the network stuff
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(getString(R.string.jwt_token));
            editor.remove(getString(R.string.plan_id));
            editor.remove(getString(R.string.trial_id));
            editor.remove(getString(R.string.user_id));
            editor.remove(getString(R.string.device_id));
            editor.apply();
        }



    }

    public static Class getActivityToStart(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.shared_prefs_tag), 0);
        boolean appHasBeenSetup = prefs.contains(c.getString(R.string.jwt_token));
        String uiToStart = prefs.getString(c.getString(R.string.UIActivityPreference), TAB_ACTIVITY);
        if(!appHasBeenSetup) {
            //the app hasn't been set up yet,
            //so fire up the Trial Runner Login Activity,
            //so a trial runner can authorise the app and receive a JWT
            return TrialRunnerLoginActivity.class;
        }
        else {


            switch (uiToStart) {
                case SINGLE_PAGE_ACTIVITY:
                    return SinglePageActivity.class;
                case TAB_ACTIVITY:
                    return TabActivity.class;
                default:
                    return TabActivity.class;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //start the BackgroundService if it's not already
        Log.i(TAG, "Starting background service...");
        startService(new Intent(this, BackgroundService.class));

        Class activity = getActivityToStart(this);
        startActivity(new Intent(this, activity));
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_prefs_tag), MODE_PRIVATE);
        //sp.unregisterOnSharedPreferenceChangeListener(PreferencesChangeListener.getInstance());
    }
}
