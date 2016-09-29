package com.medavox.repeats.background;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.medavox.repeats.R;
import com.medavox.repeats.ui.DelegatorActivity;

/**
 * @author adam
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

            Log.i(prefsChangeListener.TAG, "instance gotten from "+
                    caller.getClassName()+"."+caller.getMethodName()+":"+caller.getLineNumber());
            c = ctx;
            jwtPrefsKey = c.getString(R.string.jwt_token);
            trialIDPrefsKey = c.getString(R.string.trial_id);
            userIDPrefsKey = c.getString(R.string.user_id);
            deviceIDPrefsKey = c.getString(R.string.device_id);
            planIDPrefsKey = c.getString(R.string.plan_id);
        }
        return prefsChangeListener;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        Log.i(TAG, "shared preferences updated:" + key+"; is now "+(sp.contains(key) ? "set" : "empty"));
        if(key.equals(jwtPrefsKey)) {
            //sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            hasJWT = sp.contains(jwtPrefsKey);
        }
        else if (key.equals(trialIDPrefsKey)) {
            hasTrialID = sp.contains(trialIDPrefsKey);
        }
        else if(key.equals(userIDPrefsKey)) {
            hasUserID = sp.contains(userIDPrefsKey);
        }
        else if(key.equals(planIDPrefsKey)) {
            hasPlanID = sp.contains(planIDPrefsKey);
        }
        else if(key.equals(deviceIDPrefsKey)) {
            hasDeviceID = sp.contains(deviceIDPrefsKey);
        }

        /*
        hasJWT = sp.contains(jwtPrefsKey);
        hasTrialID = sp.contains(trialIDPrefsKey);
        hasUserID = sp.contains(userIDPrefsKey);
        hasPlanID = sp.contains(planIDPrefsKey);
        hasDeviceID = sp.contains(deviceIDPrefsKey);
*/
        Log.i(TAG, "data state: "+
                "\nhas JWT: "+hasJWT+
                "\nhas trial ID: "+hasTrialID+
                "\nhas user  ID: "+hasUserID+
                "\nhas plan  ID: "+hasPlanID+
                "\nhas deviceID: "+hasDeviceID);
        //each time SP is changed, check if this puts us in a state where we can (or should) call getPlan
        if(hasJWT && hasTrialID && hasUserID) {
            if(!hasPlanID && !hasDeviceID) {//we haven't downloaded the planID and deviceID yet,
                //but we have all the information we need in order to do that
                //get the trialID & userID from sharedPrefs
                int trialID = sp.getInt(trialIDPrefsKey, -1);
                String userID = sp.getString(userIDPrefsKey, null);
                //one last sanity check before downloading
                if (trialID != -1 && userID != null) {
                    //NetworkController.getInstance().getPlan(trialID, userID);
                    //this will eventually result in a call back to this method
                }
            }
            else if(hasPlanID && hasDeviceID) {//all variable are now set,
                //so load a normal UI activity subclass
                c.startActivity(new Intent(c, DelegatorActivity.getActivityToStart(c)));
            }
        }
    }
}
