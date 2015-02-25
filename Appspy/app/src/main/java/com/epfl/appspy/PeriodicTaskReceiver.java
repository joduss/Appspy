package com.epfl.appspy;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;


/**
 * Check
 */
public class PeriodicTaskReceiver extends BroadcastReceiver {
    private static final String TAG = "PeriodicTaskReceiver";

    private static final String EXTRA = "extra";
    private enum EXTRA_ACTION_PERIODICITY {NONE, HALF_HOUR, MINUTE, TEN_SECONDS };
    private final int NO_EXTRA = -1;

    private static Context context;

    private static ApplicationsInformation appInformation;




    public void createAlarms(Context context, Intent intent){
        Log.d("Appspy", "Alarm is set");

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        final int tenSeconds = 10000;
        final int minute = 60000;
        final int halfHour = 30000; //60000 * 30; //For now: 30 seconds
        final int CODE_ONE = 12323;
        final int CODE_TWO = 12324;

        Intent backgroundChecker;
        PendingIntent pendingIntent;


        //Ten second periodicity
        backgroundChecker = new Intent(context, PeriodicTaskReceiver.class);
        backgroundChecker.setAction(Intent.ACTION_SEND);
        backgroundChecker.putExtra(EXTRA, EXTRA_ACTION_PERIODICITY.TEN_SECONDS);
        pendingIntent = PendingIntent.getBroadcast(context, CODE_ONE, backgroundChecker, PendingIntent.FLAG_CANCEL_CURRENT);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), tenSeconds, pendingIntent);

        //Halft hour periodicity
        backgroundChecker = null;
        pendingIntent = null;
        backgroundChecker = new Intent(context, PeriodicTaskReceiver.class);
        backgroundChecker.setAction(Intent.ACTION_SEND);
        backgroundChecker.putExtra(EXTRA, EXTRA_ACTION_PERIODICITY.HALF_HOUR);
        pendingIntent = PendingIntent.getBroadcast(context, CODE_TWO, backgroundChecker, PendingIntent.FLAG_CANCEL_CURRENT);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), halfHour, pendingIntent);

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // Log.d("Appspy","INTENT IS ROCO: " + intent);
        Log.d("Appspy","ACTION: " + intent.getAction());

        //Init class members
        if(this.context == null || this.appInformation == null){
            this.context = context;
            appInformation = new ApplicationsInformation(context);
        }


        //Process the broadcast message
        if(intent.getAction() != null) {

            //Log.d("Appspy", "HEY: " + intent.getExtras().containsKey(EXTRA));


            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                // Register your reporting alarms here.
                createAlarms(context,intent);

            } else if (intent.getAction().equals(Intent.ACTION_SEND)
                    && (EXTRA_ACTION_PERIODICITY) intent.getSerializableExtra(EXTRA) == EXTRA_ACTION_PERIODICITY.TEN_SECONDS ){

                periodicCheckTenSeconds();

                Log.d("Appspy", "%%%%%%%%%%%% PERIODIC TASK every 10 seconds");

                appInformation.getCurrentlyUsedApp(false);

                List<PackageInfo> activeApps = appInformation.getActiveApps(false);

                //log



                Log.d("Appspy", "-------------------------------");
                Log.d("Appspy", "Active apps");
                Log.d("Appspy", "-------------------------------");
                for (PackageInfo app : activeApps) {
                    Log.d("Appspy", "" + app.applicationInfo.loadLabel(context.getPackageManager()));
                }
            } else if (intent.getAction().equals(Intent.ACTION_SEND)
                    && (EXTRA_ACTION_PERIODICITY) intent.getSerializableExtra(EXTRA) == EXTRA_ACTION_PERIODICITY.HALF_HOUR ){
                Log.d("Appspy", "%%%%%%%%%%%% PERIODIC TASK every 30 minutes");

                List<PackageInfo> installedApps = appInformation.getInstalledApps(false);
                Hashtable<PackageInfo, String[]> permissionsForApp = appInformation.getAppsPermissions(installedApps);

                Log.d("Appspy", "-------------------------------");
                Log.d("Appspy", "Permissions");
                Log.d("Appspy", "-------------------------------");

                for (PackageInfo app : installedApps) {
                    String[] permissions = permissionsForApp.get(app);
                    Log.d("Appspy", "" + appInformation.getAppName(app));

                    for (String p : permissions) {
                        Log.d("Appspy", p);
                    }
                    Log.d("Appspy", "===============================");

                }

            }
        }
    }


public void periodicCheckTenSeconds(){
    Log.d("Appspy","Context: " + context);
}













}