package com.epfl.appspy;

import android.app.ActivityManager;
import android.app.Application;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;


public class PeriodicTaskReceiver extends BroadcastReceiver {
    private static String TAG = "PeriodicTaskReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Appspy", "%%%%%%%%%%%% onReceive");

        getCurrentlyUsedApp(context, false);

//        List<PackageInfo> installedApps = getInstalledApps(context, false);
//        Hashtable<PackageInfo, String[]> permissionsForApp = getAppsPermissions(installedApps, context);
//        List<PackageInfo> activeApps = getActiveApps(context,false);
//
//        //log
//
//        Log.d("Appspy","-------------------------------");
//        Log.d("Appspy","Permissions");
//        Log.d("Appspy","-------------------------------");
//        for(PackageInfo app : installedApps){
//            String[] permissions = permissionsForApp.get(app);
//            Log.d("Appspy","" + app.applicationInfo.loadLabel(context.getPackageManager()));
//
//            for(String p : permissions){
//                Log.d("Appspy", p);
//            }
//            Log.d("Appspy","===============================");
//
//        }
//
//
//        Log.d("Appspy","-------------------------------");
//        Log.d("Appspy","Active apps");
//        Log.d("Appspy","-------------------------------");
//        for(PackageInfo app : activeApps){
//            Log.d("Appspy","" + app.applicationInfo.loadLabel(context.getPackageManager()));
//        }

    }


/*    private void doPeriodicTask(Context context, Application myApplication) {
        // Periodic task(s) go here ...
        Log.d("Appspy", "%%%%%%%%%%%%%% DO PER TASK");
    }


    public void restartPeriodicTaskHeartBeat(Context context, Application myApplication) {

        Log.d("Appspy", "RESTART PER TASK");
    }


    public void stopPeriodicTaskHeartBeat(Context context) {
        Log.d("Appspy", "STOP PER TASK");

    }*/


    /**
     * Returns the installed app
     *
     * @param context
     * @return List of non-system apps
     */
    public List<PackageInfo> getInstalledApps(Context context, boolean includeSystem) {
        PackageManager packageManager = context.getPackageManager();

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
     * @param context
     * @return
     */
    public Hashtable<PackageInfo, String[]> getAppsPermissions(List<PackageInfo> apps, Context context) {
        PackageManager packageManager = context.getPackageManager();

        Log.d("Appspy", "-------------------------------------------------------");
        Log.d("Appspy", "-------------------------------------------------------");
        Log.d("Appspy", "LOGGING PERMISSION OF ALL THIRD PARTY APPLICATION");

        Hashtable<PackageInfo, String[]> permissions = new Hashtable<>();

        for (PackageInfo pkg : apps) {

            Log.d("Appspy", "##########");
            Log.d("Appspy", "-" + pkg.applicationInfo.loadLabel(packageManager));

            String[] appPermissions = pkg.requestedPermissions;

            if (appPermissions == null) {
                String[] noPermission = {"No permissions"};
                appPermissions = noPermission;
            }

            permissions.put(pkg, appPermissions);

            //output log
            for (String permission : appPermissions) {
                Log.d("Appspy", permission);
            }


        }

        return permissions;
    }


    /**
     * Return all the active applications
     *
     * @param context
     * @param includeSystem set to true to include also system apps
     * @return list of active apps
     */
    public List<PackageInfo> getActiveApps(Context context, boolean includeSystem) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
        List<PackageInfo> activeApps = new ArrayList<>();

        for (ActivityManager.RunningAppProcessInfo task : tasks) {

            String[] pkgsString = task.pkgList;

            for (String pkgString : pkgsString) {


                PackageManager pm = context.getPackageManager();

                try {
                    PackageInfo packageInfo = pm.getPackageInfo(pkgString, PackageManager.GET_META_DATA);

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
     * Return the app that is currently explicitely (displayed on the screen) used by the user
     * @param context
     * @return The app the user is using now or null otherwise
     */
    public PackageInfo getCurrentlyUsedApp(Context context, boolean includeSystem) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
        List<PackageInfo> activeApps = new ArrayList<>();

        for (ActivityManager.RunningAppProcessInfo task : tasks) {

            //Check if the task is the one on foreground, thus, displayed on the screen and currently used by the user
            if(task.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                PackageManager pm = context.getPackageManager();
                String[] pkgsString = task.pkgList;

                for(String pkgName : pkgsString) {

                    try {
                        PackageInfo packageInfo = pm.getPackageInfo(pkgName, PackageManager.GET_META_DATA);

                        if ((isSystem(packageInfo) == false) || includeSystem) {
                            //exclude our app from it (because always on foreground) ???
                            if(context.getApplicationInfo().processName.equals(packageInfo.applicationInfo.processName) == false) {
                                Log.d("Appspy", "" + packageInfo.applicationInfo.loadLabel(pm));
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


    public boolean isSystem(PackageInfo pi) {
        // check if bit the for the flag "system" is 1.
        // if is NOT system, (f1 & flag_system) = 0
        //is system if (f1 & flag_system) != 0
        // (basic bitwise operation)

        return (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

}