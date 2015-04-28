package com.epfl.appspy;

import java.io.Serializable;

/**
 * Created by Jonathan Duss on 30.03.15.
 */




public class GlobalConstant implements Serializable {

    public static final boolean DEBUG = true;
    public static final boolean DEBUG_TOAST = true;
    public static final boolean LOG = true;




    /**
     * Enum for the periodicity of the tasks
     */
    public enum EXTRA_ACTION {
        MANUAL, FIRST_LAUNCH, AUTOMATIC, UPDATE
    }

    public static final String EXTRA_TAG = "task_extra";

    //public static final int GPS_PERIODICITY = 15 * 60 * 1000;
    public static final int APP_ACTIVITY_PERIODICITY_MILLIS = 60*1000;


    public static final String PREFERENCES = "prefs app spy";
    public static final String PREF_FIRST_LAUNCH = "pref first launch";
    //public static final String PREF_GPS_PERIODICITY = "pref gps periodicity";


    public static final String APPSPY_TMP_DIR = "/tmp/appspy";
    public static final String LOG_FILENAME = "appspy.log";



    public static final long APP_ACTIVITY_SAMPLING_TIME_MILLIS = 60000;
}


