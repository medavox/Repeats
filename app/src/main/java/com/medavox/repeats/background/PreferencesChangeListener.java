package com.medavox.repeats.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author Adam Howard
@date 26/09/16
 */

public class PreferencesChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    //booleans to let us know when we have all the sharedpreference values we need in order to get a plan
    private boolean hasJWT = false;
    private boolean hasTrialID = false;
    private boolean hasUserID = false;

    private boolean hasPlanID = false;
    private boolean hasDeviceID = false;

    private static PreferencesChangeListener prefsChangeListener = null;
    private String TAG = "Preferences Listener";
    private static Context c;
    private static String jwtPrefsKey;
    private static String trialIDPrefsKey;
    private static String userIDPrefsKey;
    private static String deviceIDPrefsKey;
    private static String planIDPrefsKey;

    //hidden constructor, to prevent instantiation
    private PreferencesChangeListener() {}

    //singleton
    public static PreferencesChangeListener getInstance(Context ctx) {

        if (prefsChangeListener == null) {
            prefsChangeListener = new PreferencesChangeListener();
            StackTraceElement caller = Thread.currentThread().getStackTrace()[3];

            Log.i(prefsChangeListener.TAG, "instance gotten, called from "+
                    caller.getClassName()+"."+caller.getMethodName()+":"+caller.getLineNumber());
            c = ctx;
        }
        return prefsChangeListener;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        Log.i(TAG, "shared preferences updated:" + key+"; is now "+(sp.contains(key) ? "set" : "empty"));

    }
}
