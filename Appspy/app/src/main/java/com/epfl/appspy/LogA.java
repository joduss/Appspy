package com.epfl.appspy;

import android.util.Log;

/**
 * Created by Jonathan Duss on 04.03.15.
 */
public class LogA {

    private static boolean showDBLog = false;

    public static void i(String tag, String message){

        if(tag.equals("Appspy-DB") == false || showDBLog) {
            Log.i(tag, message);
        }
    }

    public static void d(String tag, String message){
        //Log.d(tag, message);
    }
}
