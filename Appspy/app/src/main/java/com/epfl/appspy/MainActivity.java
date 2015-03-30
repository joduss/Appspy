package com.epfl.appspy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {


    private PendingIntent pendingIntent = null;
    private AlarmManager manager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



//        Intent backgroundChecker = new Intent(this, AppActivityPeriodicTaskReceiver.class);
//        pendingIntent = PendingIntent.getBroadcast(this, 0,backgroundChecker, 0);
//
//
//        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//        int interval = 10000;
//
//        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);



        this.showRightsActivity(null);


        //check if is first time the app is launched
        SharedPreferences settings = getSharedPreferences(GlobalConstant.PREFERENCES, 0);
        boolean alreadyUsed = settings.getBoolean(GlobalConstant.PREF_FIRST_LAUNCH, false);

        if(alreadyUsed == false){
            Log.i("Appspy", "First time launching Appspy");
            settings.edit().putBoolean(GlobalConstant.PREF_FIRST_LAUNCH, true);
            settings.edit().commit();

            //call the InstalledAppsReceiver to check all installed apps
            Intent installedAppReceiver = new Intent(getApplicationContext(), InstalledAppsReceiver.class);
            installedAppReceiver.setAction(Intent.ACTION_SEND);
            installedAppReceiver.putExtra(GlobalConstant.EXTRA_TAG, GlobalConstant.EXTRA_ACTION.INSTALLED_APP);
            sendBroadcast(installedAppReceiver);
        }


        //Launch GPS (useful when app is installed and launched for the first time. After that, not useful
        //the service is started with the boot.
        Intent gpsTaskReceiver = new Intent(getApplicationContext(), GPSTaskReceiver.class);
        gpsTaskReceiver.setAction(Intent.ACTION_SEND);
        gpsTaskReceiver.putExtra(GlobalConstant.EXTRA_TAG, GlobalConstant.EXTRA_ACTION.GPS);
        sendBroadcast(gpsTaskReceiver);


        Intent activityTaskReceiver = new Intent(getApplicationContext(), AppActivityPeriodicTaskReceiver.class);
        gpsTaskReceiver.setAction(Intent.ACTION_SEND);
        gpsTaskReceiver.putExtra(GlobalConstant.EXTRA_TAG, GlobalConstant.EXTRA_ACTION.APP_ACTIVITY);
        sendBroadcast(activityTaskReceiver);




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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**Show the activity showing the rights */
    public void showRightsActivity(View view){

        Intent rightsActivity = new Intent(this,RightsActivity.class);
        startActivity(rightsActivity);

    }



}
