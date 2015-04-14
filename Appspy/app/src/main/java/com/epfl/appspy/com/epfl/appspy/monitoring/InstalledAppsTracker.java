package com.epfl.appspy.com.epfl.appspy.monitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.widget.Toast;

import com.epfl.appspy.ApplicationsInformation;
import com.epfl.appspy.GlobalConstant;
import com.epfl.appspy.LogA;
import com.epfl.appspy.ToastDebug;
import com.epfl.appspy.com.epfl.appspy.database.ApplicationInstallationRecord;
import com.epfl.appspy.com.epfl.appspy.database.Database;
import com.epfl.appspy.com.epfl.appspy.database.PermissionRecord;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 * This class is responsible to monitor app installation update, or  uninstallation.
 * Each time one of these action is done, the permissions are monitored at the same time and a record is stored
 * in the database
 *
 *
 * Created by Jonathan Duss on 30.03.15.
 *
 */
public class InstalledAppsTracker extends BroadcastReceiver
{


    private static ApplicationsInformation appInformation;
    private static Context context;

    private static final boolean INCLUDE_SYSTEM = true;


    private boolean firstTimeUse = false;

    @Override
    public void onReceive(Context context, Intent intent) {
    Log.d("Appspy","on receive InstalledAppsTracker");

        //Init class members
        if (this.context == null || appInformation == null) {
            this.context = context;
            appInformation = new ApplicationsInformation(context);
        }



        if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
            LogA.i("Appspy", "A package was added");
        }
        else if(intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED)) {
            LogA.i("Appspy", "A package was updated");
        }
        else if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
            LogA.i("Appspy", "A package was removed");
        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i("Appspy", "Permission and apps installed triggered by boot");
        }
        else if(intent.getAction().equals(Intent.ACTION_SEND) &&
                intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
                GlobalConstant.EXTRA_ACTION.MANUAL){
            Log.i("Appspy", "Permission and apps installed triggered manually");
        }
        else if(intent.getAction().equals(Intent.ACTION_SEND) &&
                intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
                GlobalConstant.EXTRA_ACTION.FIRST_LAUNCH){
                firstTimeUse = true;
        }
        retrieveInstalledApps();
        firstTimeUse = false;
    }


    private void retrieveInstalledApps() {
        Log.d("Appspy", "%%%%%%%%%%%% RETRIEVE INSTALLED APP TASK");
        ToastDebug.makeText(context, "Broadcast for install app received", Toast.LENGTH_LONG).show();

        List<PackageInfo> installedApps = appInformation.getInstalledApps(INCLUDE_SYSTEM);
        Hashtable<PackageInfo, List<String>> permissionsForAllApps = appInformation.getAppsPermissions(installedApps);


        LogA.d("Appspy-loginfo", "-------------------------------");
        LogA.d("Appspy-loginfo", "Installed apps + Permissions");
        LogA.d("Appspy-loginfo", "-------------------------------");

        Database db = new Database(context);
        long currentTime = System.currentTimeMillis();


        //For each app, insert or update a record about the time of the installations/uninstallation, permissions, etc

        List<String> previousInstalledApps = db.getInstalledAppsPackageNameInLastRecord();

        for (PackageInfo app : permissionsForAllApps.keySet()) {
            List<String> permissions = permissionsForAllApps.get(app);

            LogA.d("Appspy-loginfo", appInformation.getAppName(app));
            //Log.d("Appspy-loginfo","some permissions...");
//            for (String p : permissions) {
//                Log.d("Appspy-loginfo", p);
//            }
            //Log.d("Appspy-loginfo", "===============================");

            long installationDate = app.firstInstallTime;

            String appName = appInformation.getAppName(app);
            String pkgName = app.packageName;
            boolean appSystem = appInformation.isSystem(app);

            //Add or update the record in the database
            ApplicationInstallationRecord record = new ApplicationInstallationRecord(appName, pkgName, installationDate,
                                                                                     0, appSystem);
            db.addOrUpdateApplicationInstallationRecord(record);

            //Update the permissions records for the app
            HashMap<String, PermissionRecord> permissionRecords = new HashMap<>();
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
            
            db.updatePermissionRecordsForApp(app.packageName, permissionRecords);

            previousInstalledApps.remove(app.packageName);
        }

        //Here are the uninstalled apps
        for(String packageName : previousInstalledApps){
            ApplicationInstallationRecord record = db.getApplicationInstallationRecord(packageName);
            record.setUninstallationDate(currentTime);
            db.addOrUpdateApplicationInstallationRecord(record);
        }
    }
}
