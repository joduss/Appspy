package com.epfl.appspy;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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

                //test if there are permissions. If null, then, there are no permissions and we add a String to say so
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
    public PackageInfo getUsedForegroundApp() {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
        List<PackageInfo> activeApps = new ArrayList<>();


        //FOR API < 21
        final int deprecationFrom = 21;
        if (Build.VERSION.SDK_INT < 21) {
            List<ActivityManager.RunningTaskInfo> runningTask = activityManager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo taskRunning = runningTask.get(0);

            String packageName = taskRunning.topActivity.getPackageName();

            try {
                return packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            }
            catch(PackageManager.NameNotFoundException e){
                return null;
            }

        } else {
            //FOR API > 21
            //TODO, use statistic usage
            //http://stackoverflow.com/questions/24590533/how-to-get-recent-tasks-on-android-l
            return null;
        }



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

}
