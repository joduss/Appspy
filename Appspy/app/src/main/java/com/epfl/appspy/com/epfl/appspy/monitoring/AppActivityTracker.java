package com.epfl.appspy.com.epfl.appspy.monitoring;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import com.epfl.appspy.ApplicationsInformation;
import com.epfl.appspy.GlobalConstant;
import com.epfl.appspy.LogA;
import com.epfl.appspy.ToastDebug;
import com.epfl.appspy.com.epfl.appspy.database.ApplicationActivityRecord;
import com.epfl.appspy.com.epfl.appspy.database.Database;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Process;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * Check
 */
public class AppActivityTracker extends BroadcastReceiver {


    private static boolean boot = false;

    private static final String EXTRA = GlobalConstant.EXTRA_TAG;
    private static Context context;
    ;
    private static ApplicationsInformation appInformation;

    //private final boolean INCLUDE_SYSTEM = true; //SHOULD BE TRUE UNLESS DEBUG


    private static int interval = GlobalConstant.APP_ACTIVITY_PERIODICITY_MILLIS; // in milliseconds


    private final static String PATH_CPU_WRITING = "/tmp/cpu.txt";
    private final static String PATH_CPU_READING = "/tmp/cpu-reading.txt";


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

        LogA.i("Appspy", "Next alarm at:" + cal.get(Calendar.HOUR) + "h" + cal.get(Calendar.MINUTE) + ":" +
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

                boot = true;
                analyseAppActivity();
                setupCPUMonitoring();

                Database db = new Database(context);
                db.deviceStarted();
                boot = false;
            }
            else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                //setupCPUMonitoring(); not possible to do it, as the recording is not over yet
                analyseAppActivity();

                //won't be able to monitor cpu, monitoring is not over yet. Would need to wait too long
            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     intent.getSerializableExtra(EXTRA) == GlobalConstant.EXTRA_ACTION.AUTOMATIC) {
                setupCPUMonitoring();
                analyseAppActivity();
            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     intent.getSerializableExtra(EXTRA) == GlobalConstant.EXTRA_ACTION.FIRST_LAUNCH) {
                boot = true;
                Log.d("Appspy", "AppActivity PEriodic FIRST LAUNCH");
                analyseAppActivity();
                setupCPUMonitoring();
                boot = false;
            }
            else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                     intent.getSerializableExtra(EXTRA) == GlobalConstant.EXTRA_ACTION.MANUAL) {
                //DO NOTHING
                //just setup the alarm (for consistency, no manually thing should be done here)
            }
        }
    }


    /**
     * If stat about CPU usage were just collected, we move the file so the result can be read
     * Then it will start collecting stat.
     */
    private void setupCPUMonitoring(){


        final long startTime = System.currentTimeMillis();


        ToastDebug.makeText(context, "Broadcast for app activity received", Toast.LENGTH_LONG).show();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();

        try {
            new File(path + PATH_CPU_READING).delete();
            Process p = Runtime.getRuntime().exec("mv " + path + PATH_CPU_WRITING + " " + path + PATH_CPU_READING);
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final int secondsToRun;
        if(boot){
            final int repeating = GlobalConstant.APP_ACTIVITY_PERIODICITY_MILLIS / 1000;

            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minutes = cal.get(Calendar.MINUTE);
            int seconds = cal.get(Calendar.SECOND);

            cal.set(year,month,day,hour,minutes,1);
            cal.add(Calendar.SECOND, repeating);


            long dif = cal.getTimeInMillis() - System.currentTimeMillis();
            cal.setTimeInMillis(dif);

            secondsToRun = (int) (dif / 1000 - 1);
        }
        else{
            secondsToRun = GlobalConstant.APP_ACTIVITY_PERIODICITY_MILLIS / 1000 ;
        }


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {


                Calendar c2 = Calendar.getInstance();
                c2.setTimeInMillis(System.currentTimeMillis());

                int minutes2 = c2.get(Calendar.MINUTE);
                int seconds2 = c2.get(Calendar.SECOND);
                Log.d("Appspy","CPU monitoring starts at " + minutes2 + ":" + seconds2  + " and will last " + secondsToRun + "\n");

                try {

                    StringBuilder result = new StringBuilder();
                    String line;


                    Process p = Runtime.getRuntime().exec("top -m 15 -d 1 -n " + secondsToRun);

                    //p.waitFor();
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

                    while ((line = in.readLine()) != null) {
                        result.append(line);
                        result.append('\n');
                    }
                    in.close();


                    long stopTime = System.currentTimeMillis();

                    //Since the start, it will take min 60 seconds. Then it still has 60 seconds to finish the computation
                    //and update the records in temp.
                    if(stopTime - startTime < (double)(GlobalConstant.APP_ACTIVITY_PERIODICITY_MILLIS) * 1.75){
                        //update.
                        processCPU(result.toString(), secondsToRun);

                    }
                    else {
                        //Otherwise, nothing, took too long
                        //the records stored temporarily have been already replaced
                        for(int i = 0; i < 50; i ++) {
                            //to be sure I see it if it happens
                            Log.i("Appspy", "%%%%%%%%%%%%%%%%%\n" + "ERROR, CPU stat processing took too long");
                            Log.e("Appspy", "ERROR, CPU stat processing took too long");
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }




    /**
     * Handle of the tasks that should be done often
     */
    private void analyseAppActivity() {
        Log.d("Appspy", "%%%%%%%%%%%% PERIODIC TASK Analyse apps activity");

        LogA.d("Appspy-loginfo", "-------------------------------");
        LogA.d("Appspy-loginfo", "Active apps");
        LogA.d("Appspy-loginfo", "-------------------------------");


        List<UsageStats> statistics = appInformation.getUsedForegroundApp(interval);
        PackageManager pkgManager = context.getPackageManager();


        lastAddedRecordCpuToBeAdded.clear();

        long now = System.currentTimeMillis();
        Database db = new Database(context);
        Set<String> foregroundPackageName = new HashSet<>();


        for (UsageStats stat : statistics) {
            try {
                PackageInfo pi = pkgManager.getPackageInfo(stat.getPackageName(), PackageManager.GET_META_DATA);
                foregroundPackageName.add(pi.packageName);

                final int uid = pi.applicationInfo.uid;


                //does not set if was in foreground or not, because we don't know yet
                ApplicationActivityRecord record =
                        new ApplicationActivityRecord(stat.getPackageName(),
                                                      now,
                                                      stat.getTotalTimeInForeground(),
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
                if(lastRecord != null){
                    lastForegroundTime = lastRecord.getForegroundTime();
                    lastLastUsedTime = lastRecord.getLastTimeUsed();
                }


                ApplicationActivityRecord record =
                        new ApplicationActivityRecord(pi.packageName, now, lastForegroundTime,
                                                      lastLastUsedTime,
                                                      appInformation.getUploadedDataAmount(uid),
                                                      appInformation.getDownloadedDataAmount(uid), false, boot);
                db.addApplicationActivityRecordIntelligent(record);
                lastAddedRecordCpuToBeAdded.put(pi.packageName, record);
                LogA.d("Appspy-DB", "Running process " + pi.packageName);
            }
        }

        db.close();
    }


    public void processCPU(String data, int runningTimeInSec) {

        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(System.currentTimeMillis());

        int minutes2 = c2.get(Calendar.MINUTE);
        int seconds2 = c2.get(Calendar.SECOND);

        Log.d("Appspy", "CPU stats_processing starts at " + minutes2 + ":" + seconds2 + "\n");

        HashMap<Integer, CPUInfo> cpuInfosByPid = new HashMap<>();
        HashMap<String, CPUInfo> cpuInfosByPackageName = new HashMap<>();


        StringTokenizer lineTokenizer = new StringTokenizer(data);


        while (lineTokenizer.hasMoreTokens()) {
            StringTokenizer st = new StringTokenizer(lineTokenizer.nextToken());
            if (st.countTokens() >= 3) {
                String firstToken = st.nextToken();
                if (new Scanner(firstToken).hasNextInt()) {
                    int pid = Integer.parseInt(firstToken);
                    st.nextToken(); //don't care
                    String cpuPercentage = st.nextToken();
                    String cpu = cpuPercentage.split("%")[0];
                    int cpuUsage = Integer.parseInt(cpu);

                    if (cpuInfosByPid.containsKey(pid) == false) {
                        cpuInfosByPid.put(pid, new CPUInfo(pid));
                    }
                    CPUInfo info = cpuInfosByPid.get(pid);
                    info.averageCpuUsage += cpuUsage / runningTimeInSec;

                    if (info.maxCpuUsage < cpuUsage) {
                        info.maxCpuUsage = cpuUsage;
                    }
                    //Log.d("Appspy", "PID " + pid + " used " + cpuUsage);
                }
            }

        }

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();

        Database db = new Database(context);

        //update in db
        for (ActivityManager.RunningAppProcessInfo p : tasks) {
            if (cpuInfosByPid.containsKey(p.pid)) {
                CPUInfo info = cpuInfosByPid.get(p.pid);
                cpuInfosByPackageName.put(p.processName, info);
                Log.d("Appspy", "USAGE: " + p.pid + " is " + p.processName + " and used " + info.averageCpuUsage +
                                " and max is:" + info.maxCpuUsage);

                ApplicationActivityRecord recordToUpdate = lastAddedRecordCpuToBeAdded.get(p.processName);
                lastAddedRecordCpuToBeAdded.remove(p.processName);
                if (recordToUpdate != null) {
                    recordToUpdate.setAvgCpuUsage(info.averageCpuUsage);
                    recordToUpdate.setMaxCpuUsage(info.maxCpuUsage);
                    db.updateApplicationActivityRecord(recordToUpdate);
                }
                else {
                    Log.d("Appspy","fucking null");
                }

            }
        }

        //All the other, set cpu data to 0, as not active (or not enough as cpu usage < 1, thus displayed as 0)
        for (ApplicationActivityRecord record : lastAddedRecordCpuToBeAdded.values()) {
            record.setAvgCpuUsage(0);
            record.setMaxCpuUsage(0);
            db.updateApplicationActivityRecord(record);
        }

        db.close();
        lastAddedRecordCpuToBeAdded.clear();
    }


}


