package com.epfl.appspy;

import android.app.ActivityManager;
import android.app.Application;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class RightsActivity extends ActionBarActivity {

    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rights);




//        // SHOW INSTALLED APP AND THEIR PERMISSION
//        PackageManager p = getPackageManager();
//
//        List<PackageInfo> allPkg = p.getInstalledPackages(PackageManager.GET_PERMISSIONS);
//
//        Log.d("Appspy","-------------------------------------------------------");
//        Log.d("Appspy","-------------------------------------------------------");
//        Log.d("Appspy","LOGGING PERMISSION OF ALL THIRD PARTY APPLICATION");
//        for(PackageInfo pkg :allPkg) {
//
//            //only show non-system apps
//            // check if bit the for the flag "system" is 1. If it is the case, it returns something other than 0
//            // (basic bitwise operation)
//
//            if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
//                Log.d("Appspy", "##########");
//                Log.d("Appspy","-" + pkg.applicationInfo.loadLabel(p));
//
//                String[] pi = pkg.requestedPermissions;
//
//                if(pi != null) {
//                    for (String permission : pi) {
//                        Log.d("Appspy", permission);
//                    }
//                } else {
//                    Log.d("Appspy","no permission");
//                }
//            }
//        }
//
//
//
//        // SHOW ACTIVE APPS
//
//        final boolean SHOW_SYSTEM = true; //set != 0 to show system stuff (can be interesting)
//
//        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
//
//        List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
//
//        Log.d("Appspy","#################################################\n");
//        Log.d("Appspy","ACTIVE TASKS:");
//        for(ActivityManager.RunningAppProcessInfo task : tasks){
//            //if(task.ac != null)
//
//            String[] pkgsString= task.pkgList;
//
//            for(String pkgString : pkgsString) {
//
//                PackageManager pm = getPackageManager();
//
//                try {
//
//                    PackageInfo pi = pm.getPackageInfo(pkgString, PackageManager.GET_META_DATA);
//
//                    if(((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) != SHOW_SYSTEM)
//                        Log.d("Appspy","Is running:" + pi.applicationInfo.loadLabel(pm));// + "     Package name:" + pkgString);
//
//                }
//                catch(PackageManager.NameNotFoundException e){
//
//                }
//
//                //Log.d("Appspy", task.processName);
//            }
//
//
//        }
//
//        Log.d("Appspy","-------------------------------------------------------");
//        Log.d("Appspy","-------------------------------------------------------");




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rights, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement®
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void nextPackage(View v){
/*
        PackageManager p = getPackageManager();
        List<PackageInfo> allPkg = p.getInstalledPackages(PackageManager.GET_META_DATA);
        //List<PackageInfo> packageInfos = p.getInstalledPackages(PackageManager.GET_META_DATA);

        List<PackageInfo> infos = new ArrayList<PackageInfo>();

    Log.d("Appspy","test1 " + (2& 2));
        Log.d("Appspy","test2 " + (4& 2));
        Log.d("Appspy","test3 " + (4& 4));
        Log.d("Appspy","test4 " + (8 & 1));




        //Filter third party applications
        //check if bit the for the flag is 1. If it is the case, it returns something other than 0
        // (basic bitwise operation)
        for(PackageInfo pkg :allPkg) {
            if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                infos.add(pkg);
            }
        }

        Log.d("Appspy","there are " + allPkg.size() +" system packages, but only " + infos.size() + "non system packages");


        TextView tv = (TextView) findViewById(R.id.text);

        PackageInfo ai = infos.get(index);
        //PackageInfo pi = packageInfos.get(index);

        String pname = ai.packageName;
        String appName = (String) ai.applicationInfo.loadLabel(p);


        tv.setText(pname + " - " + appName);
        PermissionInfo[] pi = ai.permissions;

        Log.d("Appspy", "App" + pname);
        if(pi != null) {
            for (PermissionInfo permission : pi) {
                Log.d("Appspy", permission.toString());
            }
        } else {
            Log.d("Appspy","no permission");
        }


//        if((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 1){
//            tv.setText(pname + " is system");
//        }


        index++;
*/

    }
}
