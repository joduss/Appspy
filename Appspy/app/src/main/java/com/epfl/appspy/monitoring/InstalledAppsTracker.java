package com.epfl.appspy.monitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.epfl.appspy.ApplicationsInformation;
import com.epfl.appspy.GlobalConstant;
import com.epfl.appspy.LogA;
import com.epfl.appspy.ToastDebug;
import com.epfl.appspy.database.ApplicationInstallationRecord;
import com.epfl.appspy.database.Database;
import com.epfl.appspy.database.PermissionRecord;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import com.epfl.appspy.GlobalConstant.EXTRA_ACTION;

/**
 * This class is responsible to monitor app installation update, or  uninstallation.
 * Each time one of these action is done, the permissions are monitored at the same time and a record is stored
 * in the database
 *
 *
 * Created by Jonathan Duss on 30.03.15.
 *
 */
public class InstalledAppsTracker extends BroadcastReceiver implements Runnable
{


    private static ApplicationsInformation appInformation;
    private static Context context;

    private static final boolean INCLUDE_SYSTEM = true;


    private boolean firstTimeUse = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        ToastDebug.makeText(context, "Broadcast for install app received -- processing", Toast.LENGTH_LONG).show();

        LogA.i("Appspy-AppTracker","App tracker start working");

        //Init class members
        if (this.context == null || appInformation == null) {
            this.context = context;
            appInformation = new ApplicationsInformation(context);
        }



        if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
            LogA.i("Appspy-AppTracker", "A package was added");
        }
        else if(intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED)) {
            LogA.i("Appspy-AppTracker", "A package was updated");
        }
        else if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
            LogA.i("Appspy-AppTracker", "A package was removed");
        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i("Appspy-AppTracker", "Permission and apps installed triggered by boot");
        }
        else if(intent.getAction().equals(Intent.ACTION_SEND) &&
                intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
                EXTRA_ACTION.MANUAL){
            Log.i("Appspy-AppTracker", "Permission and apps installed triggered manually");
        }
        else if(intent.getAction().equals(Intent.ACTION_SEND) &&
                intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
                EXTRA_ACTION.FIRST_LAUNCH){
                firstTimeUse = true;
            Log.i("Appspy-AppTracker", "AppTracker first launch on this device");
        }
        new Thread(this).start();

    }


    @Override
    public void run() {
        retrieveInstalledApps();
    }


    private void retrieveInstalledApps() {

        List<PackageInfo> installedApps = appInformation.getInstalledApps(INCLUDE_SYSTEM);
        Hashtable<PackageInfo, List<String>> permissionsForAllApps = appInformation.getAppsPermissions(installedApps);


        LogA.d("Appspy-loginfo", "-------------------------------");
        LogA.d("Appspy-loginfo", "Installed apps + Permissions");
        LogA.d("Appspy-loginfo", "-------------------------------");

        long currentTime = System.currentTimeMillis();

        Handler mainHandler = new Handler(context.getMainLooper());

        //For each app, insert or update a record about the time of the installations/uninstallation, permissions, etc
        final Database db1 = Database.getDatabaseInstance(context);
        List<String> previousInstalledApps = db1.getInstalledAppsPackageNameInLastRecord();
        db1.close();


        for (final PackageInfo app : permissionsForAllApps.keySet()) {
            List<String> permissions = permissionsForAllApps.get(app);

            LogA.d("Appspy-loginfo", "app name:" + appInformation.getAppName(app));
            //Log.d("Appspy-loginfo","some permissions...");
//            for (String p : permissions) {
//                Log.d("Appspy-loginfo", p);
//            }
            //Log.d("Appspy-loginfo", "===============================");

            long installationDate = app.firstInstallTime;

            final String appName = appInformation.getAppName(app);
            final String pkgName = app.packageName;
            boolean appSystem = appInformation.isSystem(app);

            //Add or update the record in the database
            final ApplicationInstallationRecord record = new ApplicationInstallationRecord(appName, pkgName, installationDate,
                                                                                     0, appSystem);
            //is added in DB in ~20 lines below



            //Update the permissions records for the app
            final HashMap<String, PermissionRecord> permissionRecords = new HashMap<>();
            for(String permissionName : permissions){
                PermissionRecord permRecord;
                if(firstTimeUse == true){
                    //if appspy is used for the first time, we consider that the already installed app are using the
                    //given permission since their installation date.
                    permRecord = new PermissionRecord(pkgName, permissionName, installationDate);
                } else {
                    //otherwise, use currentTime for permission first use
                    permRecord = new PermissionRecord(pkgName, permissionName, currentTime);
                }

                permissionRecords.put(permissionName, permRecord);
            }

            //Execute databa
            Runnable doOnMainThread = new Runnable() {
                @Override
                public void run() {
                    final Database db2 = Database.getDatabaseInstance(context);
                    db2.addOrUpdateApplicationInstallationRecord(record);
                    db2.updatePermissionRecordsForApp(app.packageName, permissionRecords);
                }
            };
            mainHandler.post(doOnMainThread); //execute db stuff on main thread

            previousInstalledApps.remove(app.packageName);
        }

        //Here are the uninstalled apps
        for (final String packageName : previousInstalledApps) {
            final Database db3 = Database.getDatabaseInstance(context);
            final ApplicationInstallationRecord record = db3.getApplicationInstallationRecord(packageName);
            record.setUninstallationDate(currentTime);

            Runnable doOnMainThread = new Runnable() {
                @Override
                public void run() {
                    LogA.d("Appspy", "in job " + record.getApplicationName());
                    db3.addOrUpdateApplicationInstallationRecord(record);
                    db3.updatePermissionsForUninstalledApp(packageName);
                }
            };
            mainHandler.post(doOnMainThread); //execute db stuff on main thread
        }

        firstTimeUse = false; //set it to false. So then it won't never be true
        LogA.d("Appspy-AppTracker", "AppTracker work is finished");

    }

}
