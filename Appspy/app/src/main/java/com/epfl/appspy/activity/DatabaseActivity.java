package com.epfl.appspy.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.epfl.appspy.GlobalConstant.EXTRA_ACTION;

import com.epfl.appspy.GlobalConstant;
import com.epfl.appspy.LogA;
import com.epfl.appspy.R;
import com.epfl.appspy.Utility;
import com.epfl.appspy.monitoring.AppActivityTracker;
import com.epfl.appspy.monitoring.GPSTracker;
import com.epfl.appspy.monitoring.InstalledAppsTracker;

import java.io.File;
import java.io.IOException;


public class DatabaseActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
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


    @Override
    protected void onResume() {
        super.onResume();
        LogA.i("Appspy-MainActivity", "Show database activity");
    }


    public void sendDB(View v){

        LogA.i("Appspy", "click on sendDB");
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            String zipName = "appspy.zip";

            File zippedFolder = new File(path + "/tmp/" + zipName);

            //String dbPath = "/data/data/com.epfl.appspy/databases/Appspy_database";

            File originalDB = new File("data/data/com.epfl.appspy/databases/Appspy_database");
            File copiedDB = new File(path + "/" + GlobalConstant.APPSPY_TMP_DIR + "/db.db");

            //erase if exists
            copiedDB.delete();
            zippedFolder.delete();


            //if folver /tmp does not exists, create it
            File folderToZip = new File(path + "/" + GlobalConstant.APPSPY_TMP_DIR);
            if (folderToZip.exists() == false) {
                folderToZip.mkdir();
            }


            Utility.copyFile(originalDB, copiedDB);

            Utility.zipFolder(folderToZip.getAbsolutePath(), zippedFolder.getAbsolutePath());


            Uri uriFileToSend = Uri.parse("file://" + zippedFolder.getAbsolutePath());

            String text = "";
            text += "PATH is:" + path + "\n";
            text += "PATH exits:" + new File(path).exists() + "\n";

            text += "folder to zip is:"+ folderToZip + "\n";
            text += "zip should exits does it: " + zippedFolder.exists() + "\n";
            text += "zip path is: " + zippedFolder.getAbsolutePath() + "\n";



            text += "File in the tmp folder";
            for(File ff: new File(path + "/tmp").listFiles()){
                text += "\t" + ff.getName() + "/n";
            }


            text += "\n\n file to send:"+ zippedFolder.getAbsolutePath() + "   exists:" + zippedFolder.exists();


            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
                intent.setType("application/zip");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Database");
                //intent.putExtra(Intent.EXTRA_TEXT, "Hello, here is the DB!");
                intent.putExtra(Intent.EXTRA_TEXT, text);
                LogA.d("Appspy", text);


                intent.setData(Uri.parse("mailto:zatixjo@gmail.com")); // or just "mailto:" for blank
                intent.putExtra(Intent.EXTRA_STREAM, uriFileToSend);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.*/

                LogA.d("Appspy", "before");
                startActivity(intent);
            } catch (ActivityNotFoundException e){
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Error");
                alert.setMessage("You need an email client that support attachments");
                alert.setPositiveButton("Ok", null);
                alert.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public void computeStatNow(View v){

        Utility.startLogging();

        Log.d("Appspy","Request to compute stats now");

        //call the InstalledAppsTracker to check all installed apps
        Intent installedAppReceiver = new Intent(getApplicationContext(), InstalledAppsTracker.class);
        installedAppReceiver.setAction(Intent.ACTION_SEND);
        installedAppReceiver.putExtra(GlobalConstant.EXTRA_TAG, EXTRA_ACTION.MANUAL);
        sendBroadcast(installedAppReceiver);


        //Launch GPS (useful when app is installed and launched for the first time. After that, not useful
        //the service is started with the boot.
        Intent gpsTaskReceiver = new Intent(getApplicationContext(), GPSTracker.class);
        gpsTaskReceiver.setAction(Intent.ACTION_SEND);
        gpsTaskReceiver.putExtra(GlobalConstant.EXTRA_TAG, EXTRA_ACTION.MANUAL);
        sendBroadcast(gpsTaskReceiver);


        Intent activityTaskReceiver = new Intent(getApplicationContext(), AppActivityTracker.class);
        gpsTaskReceiver.setAction(Intent.ACTION_SEND);
        gpsTaskReceiver.putExtra(GlobalConstant.EXTRA_TAG, EXTRA_ACTION.MANUAL);
        sendBroadcast(activityTaskReceiver);
    }


    public void removeTmpFiles(View v){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String zipName = "appspy.zip";

        File zippedFolder = new File(path + "/tmp/" + zipName);
        zippedFolder.delete();


        File folderToDelete = new File(path + "/" + GlobalConstant.APPSPY_TMP_DIR);
        Utility.deleteFolder(folderToDelete);

        //restart logging as it stopped when removing the file
        Utility.startLogging();

    }








}
