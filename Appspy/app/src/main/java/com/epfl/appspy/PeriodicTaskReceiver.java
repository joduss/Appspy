package com.epfl.appspy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.epfl.appspy.com.epfl.appspy.database.ApplicationInstallationRecord;
import com.epfl.appspy.com.epfl.appspy.database.Database;
import com.epfl.appspy.com.epfl.appspy.database.PermissionRecord;
import com.epfl.appspy.com.epfl.appspy.database.PermissionsJSON;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


/**
 * Check
 */
public class PeriodicTaskReceiver extends BroadcastReceiver {
    private static final String TAG = "PeriodicTaskReceiver";

    private static final String EXTRA = "extra";
    private static Context context;
    ;
    private static ApplicationsInformation appInformation;
    private final int NO_EXTRA = -1;


    private static boolean alarmSet = false;



    private final boolean INCLUDE_SYSTEM = true; //SHOULD BE TRUE UNLESS DEBUG


    private static int interval = 60000*2;


    public static void createAlarms(Context context) {


        Log.d("Appspy", "Alarm is set");

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final int repeating = 20; //this one is used now (seconds)
        interval = repeating * 1000;

        final int tenSeconds = 60000*5; // TODO PUT BACK 10000
        final int halfHour = 60000*5; //TODO 60000 * 30; //For now: 30 seconds

        final int CODE_ONE = 12323;
        final int CODE_TWO = 12324;

        Intent backgroundChecker;
        PendingIntent pendingIntent;


        //Ten second periodicity
        backgroundChecker = new Intent(context, PeriodicTaskReceiver.class);
        backgroundChecker.setAction(Intent.ACTION_SEND);
        backgroundChecker.putExtra(EXTRA, EXTRA_ACTION_PERIODICITY.TEN_SECONDS);
        pendingIntent = PendingIntent.getBroadcast(context, CODE_ONE, backgroundChecker,
                                                   PendingIntent.FLAG_CANCEL_CURRENT);


        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);

        cal.add(Calendar.SECOND, repeating);



        long millisToStart = cal.getTimeInMillis();

        //millisToStart = System.currentTimeMillis() + 10000;



        Log.d("Appspy-test","Next alarm at:" + cal.get(Calendar.HOUR) +"h" +cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));


        if(Build.VERSION.SDK_INT < 19) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, millisToStart, tenSeconds, pendingIntent);

        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, millisToStart, pendingIntent);
        }

        //Halft hour periodicity
        backgroundChecker = null;
        pendingIntent = null;
        backgroundChecker = new Intent(context, PeriodicTaskReceiver.class);
        backgroundChecker.setAction(Intent.ACTION_SEND);
        backgroundChecker.putExtra(EXTRA, EXTRA_ACTION_PERIODICITY.HALF_HOUR);
        pendingIntent = PendingIntent.getBroadcast(context, CODE_TWO, backgroundChecker,
                                                   PendingIntent.FLAG_CANCEL_CURRENT);



        if(Build.VERSION.SDK_INT < 19) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, millisToStart, halfHour, pendingIntent);
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, millisToStart, pendingIntent);
        }

    }


    @Override
    public void onReceive(Context context, Intent intent) {

        createAlarms(context);


        //Init class members
        if (this.context == null || this.appInformation == null) {
            this.context = context;
            appInformation = new ApplicationsInformation(context);
        }


        //Process the broadcast message
        if (intent.getAction() != null) {

            //Log.d("Appspy", "HEY: " + intent.getExtras().containsKey(EXTRA));

            //Executes the correct task according to the notified action in the broadcast
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                // Register your reporting alarms here.

                //periodicCheckHalfHour();
                //periodicCheckTenSeconds();


            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     (EXTRA_ACTION_PERIODICITY) intent.getSerializableExtra(EXTRA) ==
                     EXTRA_ACTION_PERIODICITY.TEN_SECONDS) {

                periodicCheckTenSeconds();

            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     (EXTRA_ACTION_PERIODICITY) intent.getSerializableExtra(EXTRA) ==
                     EXTRA_ACTION_PERIODICITY.HALF_HOUR) {
                periodicCheckHalfHour();
            }
        }

        //show message on screen to show that it is working
        Toast.makeText(context, "Broadcast received", Toast.LENGTH_LONG).show();
    }


    /**
     * Handle of the tasks that should be done every 10 seconds
     */
    private void periodicCheckTenSeconds() {
        Log.d("Appspy", "%%%%%%%%%%%% PERIODIC TASK every 10 seconds");

        List<PackageInfo> activeApps = appInformation.getActiveApps();
        PackageInfo foregroundApp = appInformation.getUsedForegroundApp(interval);


        LogA.d("Appspy-loginfo", "-------------------------------");
        LogA.d("Appspy-loginfo", "Active apps");
        LogA.d("Appspy-loginfo", "-------------------------------");

        for (PackageInfo app : activeApps) {

            Database db = new Database(context);

            String appName = appInformation.getAppName(app);
            String pkgName = app.packageName;

            long t = TrafficStats.getUidTxPackets(app.applicationInfo.uid);
            boolean isOnBackground = true;
            if (foregroundApp != null && app.packageName.equals(foregroundApp.packageName)) {
                isOnBackground = false;
                LogA.d("Appspy-loginfo", "" + app.applicationInfo.loadLabel(context.getPackageManager()) + " <----- Foreground" + " t:"+t);

            }
            else {
                LogA.d("Appspy-loginfo", "" + app.applicationInfo.loadLabel(context.getPackageManager())  + " t:"+t );
            }
            long currentTime = System.currentTimeMillis();


            //TODO: get all apps running
            // + for the one that is on foreground, set isOnBackground to false
            // maybe follow how done with finding the one on foreground. GetRunningTask puis, check is running?
            //ApplicationActivityRecord record = new ApplicationActivityRecord(pkgName, currentTime, isOnBackground);
            //db.addApplicationActivityRecord(record);


            //ApplicationActivityRecordsDatabase db = new ApplicationActivityRecordsDatabase(this.context);
            //db.addApplicationActiveTimestamp(record);
        }
    }


    /**
     * Handles the task that should be done every half hour
     */
    private void periodicCheckHalfHour() {
        Log.d("Appspy", "%%%%%%%%%%%% PERIODIC TASK every 30 minutes");

        List<PackageInfo> installedApps = appInformation.getInstalledApps(INCLUDE_SYSTEM);
        Hashtable<PackageInfo, List<String>> permissionsForAllApps = appInformation.getAppsPermissions(installedApps);


        LogA.d("Appspy-loginfo", "-------------------------------");
        LogA.d("Appspy-loginfo", "Installed apps + Permissions");
        LogA.d("Appspy-loginfo", "-------------------------------");

        Database db = new Database(this.context);

        //For each app, insert or update a record about the time of the installations/uninstallation, permissions, etc
        for (PackageInfo app : permissionsForAllApps.keySet()) {
            List<String> permissions = permissionsForAllApps.get(app);

            LogA.d("Appspy-loginfo", appInformation.getAppName(app));
            //Log.d("Appspy-loginfo","some permissions...");
//            for (String p : permissions) {
//                Log.d("Appspy-loginfo", p);
//            }
            //Log.d("Appspy-loginfo", "===============================");

            long installationDate = app.firstInstallTime;
            boolean isSystem = appInformation.isSystem(app);

            String appName = appInformation.getAppName(app);
            String pkgName = app.packageName;
            boolean appSystem = appInformation.isSystem(app);

            //Format the permission before putting it in the DB
            PermissionsJSON jsonPermissions = new PermissionsJSON(permissions);

            //Add or update the record in the database
            ApplicationInstallationRecord record = new ApplicationInstallationRecord(appName, pkgName, installationDate,
                                                                                     0, appSystem);
            db.addOrUpdateApplicationInstallationRecord(record);

            //Update the permissions records for the app
            HashMap<String, PermissionRecord> permissionRecords = new HashMap<>();
            for(String permissionName : permissions){
                PermissionRecord permRecord = new PermissionRecord(pkgName, permissionName, System.currentTimeMillis());
                permissionRecords.put(permissionName, permRecord);
            }
            db.updatePermissionRecordsForApp(app.packageName, permissionRecords);
        }
    }


    /**
     * Enum for the periodicity of the tasks
     */
    protected enum EXTRA_ACTION_PERIODICITY {
        NONE, HALF_HOUR, MINUTE, TEN_SECONDS
    }


}