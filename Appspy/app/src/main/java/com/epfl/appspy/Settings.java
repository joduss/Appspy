package com.epfl.appspy;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Jonathan Duss on 19.04.15.
 */
public class Settings {

    private static final String GPS_INTERVAL_SETTINGS_KEY = "gps interval";

    private static final int DEFAULT_GPS_INTERVAL = 2*60000;
    private static Context context;

    private static Settings settingsInstance;

    private Settings(Context context){
        this.context = context;
    }

    public static synchronized Settings getSettings(Context context){
        if(settingsInstance == null){
            settingsInstance = new Settings(context);
        }
        return settingsInstance;
    }

    public int getGPSIntervalMillis(){
        SharedPreferences settings = context.getSharedPreferences(GlobalConstant.PREFERENCES, 0);
        return settings.getInt(GPS_INTERVAL_SETTINGS_KEY, DEFAULT_GPS_INTERVAL);
    }

    public synchronized void setGPSIntervalMillis(int value){
        SharedPreferences settings = context.getSharedPreferences(GlobalConstant.PREFERENCES, 0);
        settings.edit().putInt(GPS_INTERVAL_SETTINGS_KEY, value).commit();
    }


}
