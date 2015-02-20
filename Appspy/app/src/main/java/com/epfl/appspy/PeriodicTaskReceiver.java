package com.epfl.appspy;

import android.app.Application;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class PeriodicTaskReceiver extends BroadcastReceiver {
    private static String TAG = "PeriodicTaskReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Appspy", "%%%%%%%%%%%% onReceive");
    }

    private void doPeriodicTask(Context context, Application myApplication) {
        // Periodic task(s) go here ...
        Log.d("Appspy","%%%%%%%%%%%%%% DO PER TASK");
    }

    public void restartPeriodicTaskHeartBeat(Context context, Application myApplication) {

        Log.d("Appspy","RESTART PER TASK");
    }


    public void stopPeriodicTaskHeartBeat(Context context) {
        Log.d("Appspy","STOP PER TASK");

    }

}