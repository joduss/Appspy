package com.epfl.appspy;

import android.app.ActivityManager;
import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Class providing functions to get information about the app installed on the device.
 * <p/>
 * Created by Jonathan Duss on 25.02.15.
 */
public class ApplicationsInformation {

    private Context context;
    private PackageManager packageManager = null;


    public ApplicationsInformation(Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
    }


    /**
     * Returns the installed app
     *
     * @return List of non-system apps
     */
    public List<PackageInfo> getInstalledApps(boolean includeSystem) {

        List<PackageInfo> installedApps = new ArrayList<>();

        //Filter the Intent. We want Intents that can be launched. Then, retrieve the list of Activities that
        //correspond to that criteriom.
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(intent, 0);

        for (ResolveInfo info : pkgAppsList) {
            String pkgName = info.activityInfo.packageName;
            try {
                PackageInfo pkg = packageManager.getPackageInfo(pkgName, PackageManager.GET_SERVICES);
                installedApps.add(pkg);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                //the package with the specified name has not been found
                //should never happen as we get the package name from the system
            }
        }
        return installedApps;
    }


    /**
     * Returns the associated permissions of the apps given in argument
     *
     * @param apps
     * @return
     */
    public Hashtable<PackageInfo, List<String>> getAppsPermissions(List<PackageInfo> apps) {

        Hashtable<PackageInfo, List<String>> permissions = new Hashtable<>();

        for (PackageInfo pkg : apps) {
            List<String> appPermissions = new ArrayList<>(); //Default value: no permissions

            try {
                pkg = packageManager.getPackageInfo(pkg.packageName, PackageManager.GET_PERMISSIONS);
                String permissionsForThatApp[] = pkg.requestedPermissions;

                //testAccess if there are permissions. If null, then, there are no permissions and we add a String to say so
                if (permissionsForThatApp != null) {
                    for (String pi : permissionsForThatApp) {
                        appPermissions.add(pi);
                    }
                } else {
                    appPermissions.add("No permissions");
                }

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                appPermissions.add("Error while loading permissions");
            }
            permissions.put(pkg, appPermissions);
        }
        return permissions;
    }


    /**
     * Return all the active applications, in background or foreground
     *
     * @return list of active apps
     */
    public List<PackageInfo> getActiveApps() {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();

        List<PackageInfo> installedApps = getInstalledApps(true);
        List<String> runningAppProcesses = new ArrayList<>();

        List<PackageInfo> activeApps = new ArrayList<>();

        //get the running processes
        for(ActivityManager.RunningAppProcessInfo i : tasks){
            runningAppProcesses.add(i.processName);
        }

        //Check which ones of those processes correspond to a process of one installed app
        // is excluded this way all the system processes
        for(PackageInfo app : installedApps){
            String pName = app.applicationInfo.processName;

            if(runningAppProcesses.contains(pName)){
                activeApps.add(app);
            }
        }
        return activeApps;
    }


    /**
     * Return the app that is currently displayed on the screen, used by the user
     *
     * @return The app the user is using now or null otherwise
     */
    public List<UsageStats> getUsedForegroundApp(long interval) {

        String context_usage_stats_service = "usagestats"; // = Context.USAGE_STATS_SERVICE, but this is not recognize for an unknown reason
        @SuppressWarnings("ResourceType") UsageStatsManager manager = (UsageStatsManager) context.getSystemService(context_usage_stats_service);

        List<UsageStats> statistics =
                manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis() - interval,
                                        System.currentTimeMillis());

        return statistics;
    }


    /**
     * Return if a given package belogs to the system
     *
     * @param pi the package
     * @return if the package is part of the system
     */
    public boolean isSystem(PackageInfo pi) {
        // check if bit the for the flag "system" is 1.
        // if is NOT system, (f1 & flag_system) = 0
        //is system if (f1 & flag_system) != 0
        // (basic bitwise operation)

        return (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }


    /**
     * Return the name of the application to which this package belogs to
     *
     * @param packageInfo the packageInfo
     * @return name of the app
     */
    public String getAppName(PackageInfo packageInfo) {
        return (String) packageInfo.applicationInfo.loadLabel(packageManager);
    }


    protected long getUploadedDataAmountFromFile(int uid){
        String result = "";
        try {
            File fr = new File("/proc/uid_stat/" + uid + "/tcp_snd");
            Scanner sc = new Scanner(new FileReader(fr));

            while (sc.hasNext()) {
                result += sc.nextLine();
            }
            return Long.parseLong(result);
        } catch (IOException e) {
            return 0;
        }
    }

    protected long getDownloadedDataAmountFromFile(int uid){
        String result = "";
        try {
            File fr = new File("/proc/uid_stat/" + uid + "/tcp_rcv");
            Scanner sc = new Scanner(new FileReader(fr));

            while (sc.hasNext()) {
                result += sc.nextLine();
            }
            return Long.parseLong(result);
        } catch (IOException e) {
            return 0;
        }
    }



    public long getUploadedDataAmount(int uid){
        long uploadedData = TrafficStats.getUidTxBytes(uid);
        long snd = getUploadedDataAmountFromFile(uid);
        //If TrafficStats is working, use these data, otherwise, load data from another way
        //if TrafficsStat is not working, then data will be 0.
        //So if 0, maybe, TrafficStats does not work, so use the other method. The latter will return 0
        //if indeed, no data were sent
        return (uploadedData != 0) ? uploadedData : snd;
    }

    public long getDownloadedDataAmount(int uid){
        long downloadedData = TrafficStats.getUidRxBytes(uid);
        long snd = getDownloadedDataAmountFromFile(uid);
        //If TrafficStats is working, use these data, otherwise, load data from another way
        //if TrafficsStat is not working, then data will be 0.
        //So if 0, maybe, TrafficStats does not work, so use the other method. The latter will return 0
        //if indeed, no data were sent
        return (downloadedData != 0) ? downloadedData : snd;
    }

}
