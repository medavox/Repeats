package com.medavox.repeats.application;

import android.content.Context;
import android.content.SharedPreferences;

public class Application extends android.app.Application {

    /**change this manually to change app behaviour,
     * eg to bypass network usage during a demo*/
    private final static BuildMode buildMode = BuildMode.DEV;

    private static Context mContext;
    public static final int DATABASE_VERSION = 10;
    public static final String DATABASE_NAME = "medebottle_db";

    //a reference to this is needed to prevent it being garbage collected.
    //see https://developer.android.com/guide/topics/ui/settings.html#Listening
    private static SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    public static BuildMode getBuildMode() {
        return buildMode;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }

    public enum BuildMode {
        DEBUG,
        DEMO,
        PROD,
        DEV
    }

    /**Create a strong reference to the SharedPreferences.OnSharedPreferenceChangeListener object
     * which manages SharedPreferences changes. This refernce must be created in order to prevent the
     * listener being garbage collected.see https://developer.android.com/guide/topics/ui/settings.html#Listening*/
    public static void setPrefsListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        prefsListener = listener;
    }

    public static SharedPreferences.OnSharedPreferenceChangeListener getListener() {
        return prefsListener;
    }
}
