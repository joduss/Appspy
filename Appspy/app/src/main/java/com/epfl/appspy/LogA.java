package com.epfl.appspy;

import android.util.Log;

/**
 * Created by Jonathan Duss on 04.03.15.
 */
public class LogA {

    private static final boolean DEBUG = true;
    private static final boolean showDBLog = false;

    public static void i(String tag, String message){
        //if(DEBUG && tag.equals("Appspy-log") == false) {
        if(DEBUG) {
            if (tag.equals("Appspy-DB") == false || showDBLog) {
                Log.i(tag, message);
            }
        }

        //}
        //if(tag.equals("Appspy-log")){
        //   Log.i(tag, message);
        //}
    }

    public static void d(String tag, String message){
        if(DEBUG) {
            //if (tag.equals("Appspy-log")) {
                Log.d(tag, message);
            //}
        }
    }
}


