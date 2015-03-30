package com.epfl.appspy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.epfl.appspy.com.epfl.appspy.database.ApplicationActivityRecord;
import com.epfl.appspy.com.epfl.appspy.database.Database;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Check
 */
public class AppActivityPeriodicTaskReceiver extends BroadcastReceiver {
    private static final String TAG = "AppActivityPeriodicTaskReceiver";

    private static final String EXTRA = GlobalConstant.EXTRA_TAG;
    private static Context context;
    ;
    private static ApplicationsInformation appInformation;
    private final int NO_EXTRA = -1;


    private static boolean alarmSet = false;



    private final boolean INCLUDE_SYSTEM = true; //SHOULD BE TRUE UNLESS DEBUG


    private static int interval;





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
        backgroundChecker = new Intent(context, AppActivityPeriodicTaskReceiver.class);
        backgroundChecker.setAction(Intent.ACTION_SEND);
        backgroundChecker.putExtra(EXTRA, GlobalConstant.EXTRA_ACTION.APP_ACTIVITY);
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


        Log.d("Appspy-test", "Next alarm at:" + cal.get(Calendar.HOUR) + "h" + cal.get(Calendar.MINUTE) + ":" +
                             cal.get(Calendar.SECOND));


        if(Build.VERSION.SDK_INT < 19) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, interval, pendingIntent);

        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, pendingIntent);
        }

//        //Halft hour periodicity
//        backgroundChecker = null;
//        pendingIntent = null;
//        backgroundChecker = new Intent(context, AppActivityPeriodicTaskReceiver.class);
//        backgroundChecker.setAction(Intent.ACTION_SEND);
//        backgroundChecker.putExtra(EXTRA, EXTRA_ACTION_PERIODICITY.HALF_HOUR);
//        pendingIntent =
//                PendingIntent.getBroadcast(context, CODE_TWO, backgroundChecker, PendingIntent.FLAG_CANCEL_CURRENT);
//
//
//        if (Build.VERSION.SDK_INT < 19) {
//            manager.setRepeating(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, interval, pendingIntent);
//        }
//        else {
//            manager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmInMillis, pendingIntent);
//        }
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

            //Executes the correct task according to the notified action in the broadcast
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                                         || intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {

                Database db = new Database(context);
                db.deviceStarted();
                analyseAppActivity();
            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     intent.getSerializableExtra(EXTRA) ==
                     GlobalConstant.EXTRA_ACTION.APP_ACTIVITY) {

                analyseAppActivity();

            }
//            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
//                     (EXTRA_ACTION_PERIODICITY) intent.getSerializableExtra(EXTRA) ==
//                     EXTRA_ACTION_PERIODICITY.HALF_HOUR) {
//                //periodicCheckSometimes();
//            }
        }

        //show message on screen to show that it is working
        Toast.makeText(context, "Broadcast for app activity received", Toast.LENGTH_LONG).show();
    }


    /**
     * Handle of the tasks that should be done often
     */
    private void analyseAppActivity() {
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

                long lastForegroundTime = 0;
                long lastLastUsedTime = 0;
                ApplicationActivityRecord lastRecord = db.getLastApplicationActivityRecord(pi.packageName);
                if(lastRecord != null){
                    lastForegroundTime = lastRecord.getForegroundTime();
                    lastLastUsedTime = lastRecord.getLastTimeUsed();
                }


                ApplicationActivityRecord record =
                        new ApplicationActivityRecord(pi.packageName, now, lastForegroundTime,
                                                      lastLastUsedTime,
                                                      appInformation.getUploadedDataAmount(uid),
                                                      appInformation.getDownloadedDataAmount(uid), false);
                db.addApplicationActivityRecordIntelligent(record);
                Log.d("Appspy-DB", "Running process " + pi.packageName);
            }
        }

        db.close();

    }


    /**
     * Enum for the periodicity of the tasks
     */
    protected enum EXTRA_ACTION_PERIODICITY {
        NONE, HALF_HOUR, MINUTE, TEN_SECONDS
    }


}