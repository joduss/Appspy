package com.epfl.appspy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.epfl.appspy.R;

/**
 * Created by Jonathan Duss on 19.04.15.
 */
public class Settings {

    private static final String GPS_INTERVAL_SETTINGS_KEY = "gps interval";

    private static final int DEFAULT_GPS_INTERVAL = 30000;
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

    public static long getGPSIntervalMillis(){
        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

    //settings is in second
        //long value = Long.parseLong(settings.getString(context.getResources().getString(R.string.pref_key_gps_freq), "30"));

        return 1000 ;//* value;
    }

//    public synchronized void setGPSIntervalMillis(int value){
//        SharedPreferences settings = context.getSharedPreferences(GlobalConstant.PREFERENCES, 0);
//        settings.edit().putInt(GPS_INTERVAL_SETTINGS_KEY, value).commit();
//    }


}
