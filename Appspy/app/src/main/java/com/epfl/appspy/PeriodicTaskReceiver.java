package com.epfl.appspy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.epfl.appspy.com.epfl.appspy.database.ApplicationActivityRecord;
import com.epfl.appspy.com.epfl.appspy.database.ApplicationInstallationRecord;
import com.epfl.appspy.com.epfl.appspy.database.Database;
import com.epfl.appspy.com.epfl.appspy.database.PermissionRecord;
import com.epfl.appspy.com.epfl.appspy.database.PermissionsJSON;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;


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


    private static int interval;



    ////FOR DEBUG ONLY
    public static void computeDirection(Context context){
        Log.d("Appspy", "Alarm is set");

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

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


        long nextAlarmInMillis = System.currentTimeMillis()+3000;

        //millisToStart = System.currentTimeMillis() + 10000;



        Log.d("Appspy-test","Ask for direct stat computation");
            manager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, pendingIntent);

        //Halft hour periodicity
        backgroundChecker = null;
        pendingIntent = null;
        backgroundChecker = new Intent(context, PeriodicTaskReceiver.class);
        backgroundChecker.setAction(Intent.ACTION_SEND);
        backgroundChecker.putExtra(EXTRA, EXTRA_ACTION_PERIODICITY.HALF_HOUR);
        pendingIntent = PendingIntent.getBroadcast(context, CODE_TWO, backgroundChecker,
                                                   PendingIntent.FLAG_CANCEL_CURRENT);

        manager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, pendingIntent);
        ////END FOR DEBUG ONLY

    }


    public static void createAlarms(Context context) {


        Log.d("Appspy", "Alarm is set");

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final int repeating = 60; //this one is used now (seconds)
        interval = repeating * 1000;

        //final int tenSeconds = 60000*5; // TODO PUT BACK 10000
        //final int halfHour = 60000*5; //TODO 60000 * 30; //For now: 30 seconds

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

        cal.set(year,month,day,hour,minutes,1);

        cal.add(Calendar.SECOND, repeating);



        long nextAlarmInMillis = cal.getTimeInMillis();

        //millisToStart = System.currentTimeMillis() + 10000;


        Log.d("Appspy-test", "Next alarm at:" + cal.get(Calendar.HOUR) + "h" + cal.get(Calendar.MINUTE) + ":" +
                             cal.get(Calendar.SECOND));


        if(Build.VERSION.SDK_INT < 19) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, interval, pendingIntent);

        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, pendingIntent);
        }

        //Halft hour periodicity
        backgroundChecker = null;
        pendingIntent = null;
        backgroundChecker = new Intent(context, PeriodicTaskReceiver.class);
        backgroundChecker.setAction(Intent.ACTION_SEND);
        backgroundChecker.putExtra(EXTRA, EXTRA_ACTION_PERIODICITY.HALF_HOUR);
        pendingIntent =
                PendingIntent.getBroadcast(context, CODE_TWO, backgroundChecker, PendingIntent.FLAG_CANCEL_CURRENT);


        if (Build.VERSION.SDK_INT < 19) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, interval, pendingIntent);
        }
        else {
            manager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, pendingIntent);
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

                //periodicCheckSometimes();
                //periodicCheckTenSeconds();


            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     (EXTRA_ACTION_PERIODICITY) intent.getSerializableExtra(EXTRA) ==
                     EXTRA_ACTION_PERIODICITY.TEN_SECONDS) {

                periodicCheckOften();

            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     (EXTRA_ACTION_PERIODICITY) intent.getSerializableExtra(EXTRA) ==
                     EXTRA_ACTION_PERIODICITY.HALF_HOUR) {
                periodicCheckSometimes();
            }
        }

        //show message on screen to show that it is working
        Toast.makeText(context, "Broadcast received", Toast.LENGTH_LONG).show();
    }


    /**
     * Handle of the tasks that should be done often
     */
    private void periodicCheckOften() {
        Log.d("Appspy", "%%%%%%%%%%%% PERIODIC TASK often");


        LogA.d("Appspy-loginfo", "-------------------------------");
        LogA.d("Appspy-loginfo", "Active apps");
        LogA.d("Appspy-loginfo", "-------------------------------");



        List<UsageStats> statistics = appInformation.getUsedForegroundApp(interval);
        PackageManager pkgManager = context.getPackageManager();

        Log.d("Appspy", "number of US: " + statistics.size());

        long now = System.currentTimeMillis();
        Database db = new Database(context);
        Set<String> foregroundPackageName = new HashSet<>();

        for (UsageStats stat : statistics) {
            try {
                PackageInfo pi = pkgManager.getPackageInfo(stat.getPackageName(), PackageManager.GET_META_DATA);
                foregroundPackageName.add(pi.packageName);

                //BEGIN DEBUG
//                long downloadedData = TrafficStats.getUidRxBytes(pi.applicationInfo.uid);
//                long uploadedData = TrafficStats.getUidTxBytes(pi.applicationInfo.uid);
//
//                SimpleDateFormat f2 = new SimpleDateFormat("m:s");
//                SimpleDateFormat f = new SimpleDateFormat("k:m:s");
//
//                Date d1 = new Date(stat.getLastTimeUsed());
//                //Date d2 = new Date(stat.getFirstTimeStamp());
//                //Date d3 = new Date(stat.getLastTimeStamp());
//
//                long snd = appInformation.getUploadedDataAmountFromFile(pi.applicationInfo.uid);
//                long rcv = appInformation.getDownloadedDataAmountFromFile(pi.applicationInfo.uid);
//
//                Log.d("Appspy", "snd egal?" + (snd == uploadedData) + "   \t" + snd + "|\t" + uploadedData);
//                Log.d("Appspy", "rcv egal?" + (rcv == downloadedData) + "   \t" + rcv + "|\t" + downloadedData);
//
//
//                Log.d("Appspy", "Hello " + appInformation.getAppName(pi) + " - foreground is " +
//                                f2.format(stat.getTotalTimeInForeground()) + " - last used is " + f.format(d1));
                //END DEBUG

                final int uid = pi.applicationInfo.uid;

                ApplicationActivityRecord record =
                        new ApplicationActivityRecord(stat.getPackageName(), now, stat.getTotalTimeInForeground(),
                                                      stat.getLastTimeUsed(), appInformation.getUploadedDataAmount(uid),
                                                      appInformation.getDownloadedDataAmount(uid));
                db.addApplicationActivityRecordIntelligent(record);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        //now also get the info about background processes
        List<PackageInfo> runningApps = appInformation.getActiveApps();

        //need to exclude already processed apps

        //if the app is not in the stat, then the foreground time for that day is 0
        //that means the app may have background task running even if not open by the user
        for (PackageInfo pi : runningApps) {
            if (foregroundPackageName.contains(pi.packageName) == false) {
                final int uid = pi.applicationInfo.uid;
                ApplicationActivityRecord record = new ApplicationActivityRecord(pi.packageName, now, 0, 0,
                                                                                 appInformation.getUploadedDataAmount(
                                                                                         uid),
                                                                                 appInformation.getDownloadedDataAmount(
                                                                                         uid), false);
                db.addApplicationActivityRecordIntelligent(record);
            }
        }

    }


    /**
     * Handles the task that should be done not too often
     */
    private void periodicCheckSometimes() {
        Log.d("Appspy", "%%%%%%%%%%%% PERIODIC TASK sometimes");

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