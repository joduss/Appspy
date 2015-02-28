package com.epfl.appspy;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
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
        *
        * DEBUG CODE ONLY
         */
        Context context = getApplicationContext();
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final String EXTRA = "extra";


        final int tenSeconds = 10000;
        final int minute = 60000;
        final int halfHour = 30000; //60000 * 30; //For now: 30 seconds
        final int CODE_ONE = 12323;
        final int CODE_TWO = 12324;

        Intent backgroundChecker;
        PendingIntent pendingIntent;

        //Halft hour periodicity
        backgroundChecker = null;
        pendingIntent = null;
        backgroundChecker = new Intent(context, PeriodicTaskReceiver.class);
        backgroundChecker.setAction(Intent.ACTION_SEND);
        backgroundChecker.putExtra(EXTRA, PeriodicTaskReceiver.EXTRA_ACTION_PERIODICITY.HALF_HOUR);
        pendingIntent = PendingIntent.getBroadcast(context, CODE_TWO, backgroundChecker,
                                                   PendingIntent.FLAG_CANCEL_CURRENT);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), halfHour, pendingIntent);

        Log.d("Appspy","RESET ALARM");
        //Ten second periodicity
        backgroundChecker = new Intent(context, PeriodicTaskReceiver.class);
        backgroundChecker.setAction(Intent.ACTION_SEND);
        backgroundChecker.putExtra(EXTRA, PeriodicTaskReceiver.EXTRA_ACTION_PERIODICITY.TEN_SECONDS);
        pendingIntent = PendingIntent.getBroadcast(context, CODE_ONE, backgroundChecker,
                                                   PendingIntent.FLAG_CANCEL_CURRENT);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), tenSeconds, pendingIntent);

        /*
        *
        * END DEBUG CODE ONLY
         */

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> info = activityManager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo i : info){
            Log.d("Appspy-2", i.processName);
        }

    }
}
