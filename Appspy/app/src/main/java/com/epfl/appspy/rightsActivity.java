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
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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


    /*
     * Test access to some folder, and show its content.
     * => Try with shell command
     * => Try with shell command as root (only works if device rooted and allows root to apps)
     * => Try using File(path)
     */
    public void testAccess(View v){

        //Get path from user input
        EditText tv = (EditText) findViewById(R.id.tvPath);
        String path = tv.getText().toString();



        //TEST TO ACCESS PRIVATE DATA
        try {
            Process t = Runtime.getRuntime().exec(path); //execute shell command input by use

            Process t2 = Runtime.getRuntime().exec("mkdir /sdcard/test"); //execute shell command input by user


            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("ls /data/data > /sdcard/a.txt\n"); //Write a file
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(t.getInputStream()) );
            String line;

            Log.d("Appspy","%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            while ((line = in.readLine()) != null) {
                Log.d("Appspy", line);
            }
            in.close();

            //Process root2 = Runtime.getRuntime().exec("ls /data");

            //Process root2 = Runtime.getRuntime().exec("cat a.txt");
            //Log.d("Appspy", " read " + root2.getInputStream().read());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.d("Appspy","ERROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOR");
        }



        //Test to access files, without ROOT permissions


        File f = new File(path);

        Log.d("Appspy","===============================================");
        Log.d("Appspy","Content of the folder " + f.getAbsolutePath() + " | exists? " + f.exists());
        File files[] = f.listFiles();
        if(files != null){
            for (File file : files) {
                Log.d("Appspy", file.toString());
            }
        }
        else {
            Log.d("Appspy","NO FILES - NO FILES");
        }

    }


    public void nextPackage(View v){

        /*
        *
        * DEBUG CODE ONLY
         */
            PeriodicTaskReceiver.createAlarms(getApplicationContext());


        /*
        *
        * END DEBUG CODE ONLY
         */

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> info = activityManager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo i : info){
            Log.d("Appspy-2", i.processName);
        }

    }
}
