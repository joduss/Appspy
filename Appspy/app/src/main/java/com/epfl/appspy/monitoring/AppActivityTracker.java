package com.epfl.appspy.monitoring;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.usage.UsageStats;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.epfl.appspy.ApplicationsInformation;
import com.epfl.appspy.GlobalConstant.EXTRA_ACTION;
import com.epfl.appspy.GlobalConstant;
import com.epfl.appspy.LogA;
import com.epfl.appspy.activity.MainActivity;
import com.epfl.appspy.R;
import com.epfl.appspy.Utility;
import com.epfl.appspy.database.ApplicationActivityRecord;
import com.epfl.appspy.database.Database;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Check
 */
public class AppActivityTracker extends BroadcastReceiver {


    private static boolean boot = false;

    private static final String EXTRA = GlobalConstant.EXTRA_TAG;
    private static Context context;
    private static ApplicationsInformation appInformation;

    private final static int NOTIFICATION_ID = 382383;

    //private final boolean INCLUDE_SYSTEM = true; //SHOULD BE TRUE UNLESS DEBUG


    private static int interval = GlobalConstant.APP_ACTIVITY_PERIODICITY_MILLIS; // in milliseconds



    //private final static String PATH_CPU_WRITING = "/tmp/cpu.txt";
    //private final static String PATH_CPU_READING = "/tmp/cpu-reading.txt";


    HashMap<String, ApplicationActivityRecord> lastAddedRecordCpuToBeAdded = new HashMap<>();



    public static void createAlarms(Context context) {

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final int repeating = GlobalConstant.APP_ACTIVITY_PERIODICITY_MILLIS / 1000; //this one is used now (seconds)



        final int CODE_ONE = 12323;

        Intent backgroundChecker;
        PendingIntent pendingIntent;

        backgroundChecker = new Intent(context, AppActivityTracker.class);
        backgroundChecker.setAction(Intent.ACTION_SEND);
        backgroundChecker.putExtra(EXTRA, GlobalConstant.EXTRA_ACTION.AUTOMATIC);
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

        LogA.i("AppActivityTracker", "Next alarm at:" + cal.get(Calendar.HOUR) + "h" + cal.get(Calendar.MINUTE) + ":" +
                             cal.get(Calendar.SECOND));

        if(Build.VERSION.SDK_INT < 19) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, interval, pendingIntent);

        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, pendingIntent);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        //Create the "alarm", to be sure this will be called again in the future
        createAlarms(context);
        Log.d("Appspy", "%%%%%%%%%%%% APP ACTIVITY onReceive");

        //Init class members
        if (this.context == null || this.appInformation == null) {
            this.context = context;
            appInformation = new ApplicationsInformation(context);
        }

        //Process the broadcast message
        if (intent.getAction() != null) {

            //Executes the correct task according to the notified action in the broadcast
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                startLogging();
                boot = true;
                analyseAppActivity();
                //setupCPUMonitoring();

                Database db = Database.getDatabaseInstance(context);
                db.deviceStarted();
                boot = false;

            }
            else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                //setupCPUMonitoring(); not possible to do it, as the recording is not over yet
                analyseAppActivity();

                //won't be able to monitor cpu, monitoring is not over yet. Would need to wait too long
            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     intent.getSerializableExtra(EXTRA) == EXTRA_ACTION.AUTOMATIC) {
                //setupCPUMonitoring();
                analyseAppActivity();
            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     intent.getSerializableExtra(EXTRA) == EXTRA_ACTION.FIRST_LAUNCH) {
                boot = true;
                LogA.i("AppActivityTracker", "AppActivityTracker FIRST LAUNCH");

                //First don't downloaded data, to avoid having all data of the day taken as the download over 1 minutes
                //because app does not know what was the amount of downloaded data 1 minutes before, so can't compute
                List<PackageInfo> runningApps = appInformation.getActiveApps();

                Database db = Database.getDatabaseInstance(context);

                for(PackageInfo pi : runningApps){
                    final int uid = pi.applicationInfo.uid;
                    db.setLastActivity(pi.packageName,
                                       appInformation.getUploadedDataAmount(uid),
                                       appInformation.getDownloadedDataAmount(uid));
                }


                analyseAppActivity();
                //setupCPUMonitoring();
            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     intent.getSerializableExtra(EXTRA) == EXTRA_ACTION.MANUAL) {
                startLogging();
                //DO NOTHING
                //just setup the alarm (for consistency, no manually thing should be done here)
            }
        }
    }

private void startLogging(){
    Utility.startLogging();
}


    /**
     * Handle of the tasks that should be done often
     */
    private void analyseAppActivity() {
        Log.d("Appspy", "%%%%%%%%%%%% PERIODIC TASK Analyse apps activity");

        LogA.d("Appspy-loginfo", "-------------------------------");
        LogA.d("Appspy-loginfo", "Active apps");
        LogA.d("Appspy-loginfo", "-------------------------------");
        
        LogA.i("Appspy-AppActivityTracker", "Start analysing apps activity");

        if(Utility.usageStatsPermissionGranted(context) == false){
            showNotificationForUsageStatsPermission();
            LogA.d("Appspy-AppActivityTracker","Does not has permission for stats" );
            
        }
        else {


            List<UsageStats> statistics = appInformation.getUsedForegroundApp(interval);
            PackageManager pkgManager = context.getPackageManager();


            lastAddedRecordCpuToBeAdded.clear();

            long now = System.currentTimeMillis();
            Database db = Database.getDatabaseInstance(context);
            Set<String> foregroundPackageName = new HashSet<>();


            for (UsageStats stat : statistics) {
                try {
                    PackageInfo pi = pkgManager.getPackageInfo(stat.getPackageName(), PackageManager.GET_META_DATA);
                    foregroundPackageName.add(pi.packageName);

                    final int uid = pi.applicationInfo.uid;


                    //does not set if was in foreground or not, because we don't know yet
                    ApplicationActivityRecord record = new ApplicationActivityRecord(stat.getPackageName(), now, stat.getTotalTimeInForeground(),
                                                                                     stat.getLastTimeUsed(),
                                                                                     appInformation.getUploadedDataAmount(uid),
                                                                                     appInformation.getDownloadedDataAmount(uid), boot);
                    db.addApplicationActivityRecordIntelligent(record);

                    lastAddedRecordCpuToBeAdded.put(stat.getPackageName(), record);

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }


            //now also get the info about background processes

            //all apps actives
            List<PackageInfo> runningApps = appInformation.getActiveApps();


            //if the app is not in the stat, then the foreground time for that day is 0
            //but the app may have background task running even if not open by the user
            for (PackageInfo pi : runningApps) {

                //check if was not already processed as foreground app
                if (foregroundPackageName.contains(pi.packageName) == false) {
                    final int uid = pi.applicationInfo.uid;

                    long lastForegroundTime = 0;
                    long lastLastUsedTime = 0;

                    //if was in foreground before, but still running in background: get the last known foreground time
                    ApplicationActivityRecord lastRecord = db.getLastApplicationActivityRecord(pi.packageName);
                    if (lastRecord != null) {
                        lastForegroundTime = lastRecord.getForegroundTime();
                        lastLastUsedTime = lastRecord.getLastTimeUsed();
                    }


                    ApplicationActivityRecord record =
                            new ApplicationActivityRecord(pi.packageName, now, lastForegroundTime, lastLastUsedTime,
                                                          appInformation.getUploadedDataAmount(uid), appInformation.getDownloadedDataAmount(uid), false, boot);
                    db.addApplicationActivityRecordIntelligent(record);
                    //lastAddedRecordCpuToBeAdded.put(pi.packageName, record);
                    LogA.d("Appspy-DB", "Running process " + pi.packageName);
                }
            }

            db.close();
        }

        LogA.i("Appspy-AppActivityTracker", "Finished analysing apps activity");

    }



    private void showNotificationForUsageStatsPermission() {

        LogA.d("Appspy-AppActivityTracker", "Show notification to user to grant access to Usage Stats");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context).setContentTitle("Appspy").setContentText(
                        "Appspy need your help to work properly").setSmallIcon(R.drawable.ic_stat_name).setPriority(
                        NotificationCompat.PRIORITY_MAX);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);


        Notification n = mBuilder.build();
        n.defaults = Notification.DEFAULT_ALL;

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(8383, n);
    }

}


