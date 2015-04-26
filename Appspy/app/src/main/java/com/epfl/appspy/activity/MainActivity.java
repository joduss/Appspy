package com.epfl.appspy.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.epfl.appspy.GlobalConstant;
import com.epfl.appspy.LogA;
import com.epfl.appspy.R;
import com.epfl.appspy.Utility;
import com.epfl.appspy.monitoring.AppActivityTracker;
import com.epfl.appspy.monitoring.GPSTracker;
import com.epfl.appspy.monitoring.InstalledAppsTracker;
import com.epfl.appspy.GlobalConstant.EXTRA_ACTION;


public class MainActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {




    @Override
    protected void onResume() {
        super.onResume();

        LogA.i("Appspy-MainActivity", "Show main activity");
        

        //ASK FOR PERMISSION USAGE ACCESS
        checkUsageStatAccessPermission();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);


        //check if is first time the app is launched (manually)
        SharedPreferences settings = getSharedPreferences(GlobalConstant.PREFERENCES, 0);
        boolean firstLaunch = settings.getBoolean(GlobalConstant.PREF_FIRST_LAUNCH, true);


        //In which case, the monitoring tasks are directly started
        //If this is not the case, then appspy will be started automatically once the device is booted.
        if(firstLaunch){


            Log.i("Appspy", "First time launching Appspy");
            SharedPreferences.Editor settingsEditor = settings.edit();
            settingsEditor.putBoolean(GlobalConstant.PREF_FIRST_LAUNCH, false);
            settingsEditor.commit();

            //call the InstalledAppsTracker to check all installed apps
            Intent installedAppTracker = new Intent(getApplicationContext(), InstalledAppsTracker.class);
            installedAppTracker.setAction(Intent.ACTION_SEND);
            installedAppTracker.putExtra(GlobalConstant.EXTRA_TAG, EXTRA_ACTION.FIRST_LAUNCH);
            sendBroadcast(installedAppTracker);


            //Launch GPS (useful when app is installed and launched for the first time. After that, not useful
            //the service is started with the boot.
            Intent gpsTracker = new Intent(getApplicationContext(), GPSTracker.class);
            gpsTracker.setAction(Intent.ACTION_SEND);
            gpsTracker.putExtra(GlobalConstant.EXTRA_TAG, EXTRA_ACTION.FIRST_LAUNCH);
            sendBroadcast(gpsTracker);


            Intent appActivityTracker = new Intent(getApplicationContext(), AppActivityTracker.class);
            appActivityTracker.setAction(Intent.ACTION_SEND);
            appActivityTracker.putExtra(GlobalConstant.EXTRA_TAG, EXTRA_ACTION.FIRST_LAUNCH);
            sendBroadcast(appActivityTracker);
        }

        //this.showRightsActivity(null);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsActivity = new Intent(this,SettingsActivity.class);
            startActivity(settingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**Show the activity showing the rights */
    public void goDBActivity(View view){

        Intent nextActivity = new Intent(this,DatabaseActivity.class);
        startActivity(nextActivity);

    }


    public void checkUsageStatAccessPermission(){



        if(Utility.usageStatsPermissionGranted(getApplicationContext()) == false){


            LogA.d("Appspy-log","User went to settings. Hope he granted the access to Usage Stats");
            final Intent newIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            boolean activityExists = newIntent.resolveActivityInfo(getPackageManager(), 0) != null;


            if(activityExists) {
                LogA.i("Appspy-log", "Ask user to go in settings to grant permission to Usage Stats");
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("We need you to grant Usage access");

                alert.setPositiveButton(R.string.show_usage_permission_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(newIntent);
                    }
                });
                alert.create().show();

            }
            else
            {
                final Intent securityIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                securityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                boolean activitySecurityExists = securityIntent.resolveActivityInfo(getPackageManager(), 0) != null;

                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("We need you to grant Usage access.");
                alert.setMessage("Go to \"Settings\" -> \"Security\" -> \"Apps with usage access\" and authorize Appspy");

                if(activitySecurityExists) {


                    alert.setPositiveButton(R.string.show_usage_permission_settings, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    startActivity(securityIntent);
                                                }
                                            });
                    alert.create().show();
                }
                else {
                    alert.setPositiveButton(R.string.show_usage_permission_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nothing just close the notif. Hope the user will follow the instruction
                        }
                    });
                    alert.create().show();
                }
            }
        }

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Launch GPS (useful when app is installed and launched for the first time. After that, not useful
        //the service is started with the boot.

        if (key.equals(getResources().getString(R.string.pref_key_gps_freq))) {

            Intent gpsTracker = new Intent(getApplicationContext(), GPSTracker.class);
            gpsTracker.setAction(Intent.ACTION_SEND);
            gpsTracker.putExtra(GlobalConstant.EXTRA_TAG, EXTRA_ACTION.UPDATE);
            sendBroadcast(gpsTracker);

            long newInterval = com.epfl.appspy.Settings.getSettings(getApplicationContext()).getGPSIntervalMillis();
            LogA.i("Appspy-MainActivity", "Settings for GPS interval changed to " + newInterval / 1000 + " seconds");
        }

    }


}
