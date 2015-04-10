package com.epfl.appspy;

/**
 * Created by Jonathan Duss on 30.03.15.
 */
public class GlobalConstant {

    public static final boolean DEBUG = true;

    /**
     * Enum for the periodicity of the tasks
     */
    public enum EXTRA_ACTION {
        MANUAL, FIRST_LAUNCH, AUTOMATIC
    }

    public static final String EXTRA_TAG = "task_extra";

    //public static final int GPS_PERIODICITY = 15 * 60 * 1000;
    public static final int APP_ACTIVITY_PERDIOCITY = 60*1000;


    public static final String PREFERENCES = "prefs app spy";
    public static final String PREF_FIRST_LAUNCH = "pref first launch";
    public static final String PREF_GPS_PERIODICITY = "pref gps periodicity";
}


