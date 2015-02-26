package com.epfl.appspy;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Class providing functions to get information about the app installed on the device.
 *
 * Created by Jonathan Duss on 25.02.15.
 */
public class ApplicationsInformation {

    private Context context;
    private PackageManager packageManager = null;

    public ApplicationsInformation(Context context){
        this.context = context;
        packageManager = context.getPackageManager();
    }

    /**
     * Returns the installed app
     *
     * @return List of non-system apps
     */
    public List<PackageInfo> getInstalledApps(boolean includeSystem) {

        List<PackageInfo> allPkg = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        List<PackageInfo> installedApps = new ArrayList<>();

        for (PackageInfo pkg : allPkg) {

            //we add the app to the list if it is not system OR if we include system apps
            if ((isSystem(pkg) == false) || includeSystem) {
                installedApps.add(pkg);
            }
        }

        return installedApps;



        //Try get currently used app


    }

    /**
     * Returns the associated permissions of the apps given in argument
     *
     * @param apps
     * @return
     */
    public Hashtable<PackageInfo, String[]> getAppsPermissions(List<PackageInfo> apps) {

        Hashtable<PackageInfo, String[]> permissions = new Hashtable<>();

        for (PackageInfo pkg : apps) {

            String[] appPermissions = pkg.requestedPermissions;

            if (appPermissions == null) {
                String[] noPermission = {"No permissions"};
                appPermissions = noPermission;
            }

            permissions.put(pkg, appPermissions);
        }

        return permissions;
    }

    /**
     * Return all the active applications, in background or foreground
     *
     * @param includeSystem set to true to include also system apps
     * @return list of active apps
     */
    public List<PackageInfo> getActiveApps(boolean includeSystem) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
        List<PackageInfo> activeApps = new ArrayList<>();


        for (ActivityManager.RunningAppProcessInfo task : tasks) {

            String[] pkgsString = task.pkgList;

            for (String pkgString : pkgsString) {

                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(pkgString, PackageManager.GET_META_DATA);

                    //we add the app to the list if it is not system OR if we include system apps
                    if ((isSystem(packageInfo) == false) || includeSystem) {
                        activeApps.add(packageInfo);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    //nothing to do
                }
            }
        }
        return activeApps;
    }


    /**
     * Return the app that is currently displayed on the screen, used by the user
     * @return The app the user is using now or null otherwise
     */
    public PackageInfo getCurrentlyUsedApp(boolean includeSystem) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> tasks = activityManager.getRunningServices(100000);
        List<PackageInfo> activeApps = new ArrayList<>();

        for (ActivityManager.RunningServiceInfo task : tasks) {

            //Check if the task is the one on foreground, thus, displayed on the screen and currently used by the user
            if(task.foreground == true){
                String[] pkgsString = tasks(1).;

                for(String pkgName : pkgsString) {

                    try {
                        PackageInfo packageInfo = packageManager.getPackageInfo(pkgName, PackageManager.GET_META_DATA);

                        if ((isSystem(packageInfo) == false) || includeSystem) {
                            //exclude our app from it (because always on foreground) ???
                            if(context.getApplicationInfo().processName.equals(packageInfo.applicationInfo.processName) == false) {
                                Log.d("Appspy", "" + getAppName(packageInfo));
                                return packageInfo;
                            }
                        }

                    } catch (PackageManager.NameNotFoundException e) {
                        //nothing to do
                    }
                }
            }
        }
        return null;
    }


    /**
     * Return if a given package belogs to the system
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
     * @param packageInfo the packageInfo
     * @return name of the app
     */
    public String getAppName(PackageInfo packageInfo){
        return (String) packageInfo.applicationInfo.loadLabel(packageManager);
    }

}
