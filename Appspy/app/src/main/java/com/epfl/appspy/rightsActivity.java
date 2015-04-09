package com.epfl.appspy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.epfl.appspy.com.epfl.appspy.monitoring.AppActivityPeriodicTaskReceiver;
import com.epfl.appspy.com.epfl.appspy.monitoring.GPSTaskReceiver;
import com.epfl.appspy.com.epfl.appspy.monitoring.InstalledAppsReceiver;

import java.io.File;
import java.io.IOException;


public class RightsActivity extends ActionBarActivity {


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
//            Process t = Runtime.getRuntime().exec(path); //execute shell command input by use
//
//            Process t2 = Runtime.getRuntime().exec("mkdir /sdcard/test"); //execute shell command input by user
//
//
//            Process su = Runtime.getRuntime().exec("su");
//            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
//
//            outputStream.writeBytes("ls /data/data > /sdcard/a.txt\n"); //Write a file
//            outputStream.flush();
//
//            outputStream.writeBytes("exit\n");
//            outputStream.flush();
//            su.waitFor();
//
//
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(t.getInputStream()) );
//            String line;
//
//            Log.d("Appspy","%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//            while ((line = in.readLine()) != null) {
//                Log.d("Appspy", line);
//            }
//            in.close();
//
//            //Process root2 = Runtime.getRuntime().exec("ls /data");
//
            Process root2 = Runtime.getRuntime().exec("cp /data/data/com.epfl.appspy/databases/Appspy_database /sdcard/tmp/aure.db");
//            //Log.d("Appspy", " read " + root2.getInputStream().read());
//
        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d("Appspy","ERROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOR");
        }



        //Test to access files, without ROOT permissions
//
//
//        File f = new File(path);
//
//        Log.d("Appspy","===============================================");
//        Log.d("Appspy","Content of the folder " + f.getAbsolutePath() + " | exists? " + f.exists());
//        File files[] = f.listFiles();
//        if(files != null){
//            for (File file : files) {
//                Log.d("Appspy", file.toString());
//            }
//        }
//        else {
//            Log.d("Appspy","NO FILES - NO FILES");
//        }



        //DEBUG
//        ApplicationsInformation appInformation = new ApplicationsInformation(getApplicationContext());
//        List<UsageStats> statistics = appInformation.getUsedForegroundApp(60000);
//        PackageManager pkgManager = getApplicationContext().getPackageManager();
//
//        SimpleDateFormat f2 = new SimpleDateFormat("m:s");
//        SimpleDateFormat f3 = new SimpleDateFormat("k:m:s");

//        TextView textView = (TextView) findViewById(R.id.textview);
//
//        String t = "";
//        textView.setText("");
//        for (UsageStats stat : statistics){
//            long lastUsed = stat.getLastTimeUsed();
//            Date d1 = new Date(stat.getLastTimeUsed());
//
//            try {
//                PackageInfo pi = pkgManager.getPackageInfo(stat.getPackageName(), PackageManager.GET_META_DATA);
//
//                long downloadedData = TrafficStats.getUidRxBytes(pi.applicationInfo.uid);
//                long uploadedData = TrafficStats.getUidTxBytes(pi.applicationInfo.uid);
//                long snd = appInformation.getUploadedDataAmount(pi.applicationInfo.uid);
//                long rcv = appInformation.getDownloadedDataAmount(pi.applicationInfo.uid);
//
//                Log.d("Appspy", "uploaded TS:?" + uploadedData + "| file: " + snd);
//                Log.d("Appspy", "downloaded TS:?" + downloadedData + "| file: " + rcv);
//
//                double coefDiv =  Math.pow(1024,2);
//
//                DecimalFormat df = new DecimalFormat("#.###");
//
//
//                t = t + "================================= \n";
//                t = t + pi.packageName + "data in MB" + "\n";
//                t = t + "uploaded TS: " + df.format(uploadedData/coefDiv) + "| file: " + df.format(snd/coefDiv) + "\n";
//                t= t + "downloaded TS: " + df.format(downloadedData/coefDiv) + "| file: " + df.format(rcv/coefDiv) + "\n";
//
//            }
//            catch(PackageManager.NameNotFoundException e){
//                System.err.println("##############\n This error should not happen. If it happens, try to see why!!");
//                e.printStackTrace();
//                System.exit(1);
//            }
//        }
//        textView.setText(t);
//        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//        clipboard.setText(t);

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }


    }


    public void sendDB(View v){
        try {

            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File f = new File(path + "/tmp");
            if (f.exists() == false) {
                f.mkdir();
            }


            Process root2 = Runtime.getRuntime().exec(
                    "cp /data/data/com.epfl.appspy/databases/Appspy_database " + path + "/tmp/data2.zip");

            String fileDB = path + "/tmp/data2.zip";

            File f2 = new File(fileDB);
            Log.d("Appspy", "exits: " + f2.exists());
            Log.d("Appspy", "exits2: " + new File(path).exists());


            Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
            intent.setType("file/*");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Database");
            intent.putExtra(Intent.EXTRA_TEXT, "Hello, here is the DB!");
            //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:/" + fileDB));
            intent.setData(Uri.parse("mailto:zatixjo@gmail.com")); // or just "mailto:" for blank
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
            startActivity(intent);

            //Process root3 = Runtime.getRuntime().exec("rm /sdcard/tmp/database.db");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void computeStatNow(View v){


        //call the InstalledAppsReceiver to check all installed apps
        Intent installedAppReceiver = new Intent(getApplicationContext(), InstalledAppsReceiver.class);
        installedAppReceiver.setAction(Intent.ACTION_SEND);
        installedAppReceiver.putExtra(GlobalConstant.EXTRA_TAG, GlobalConstant.EXTRA_ACTION.INSTALLED_APP);
        sendBroadcast(installedAppReceiver);


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



        public void nextPackage(View v){

        /*
        *
        * DEBUG CODE ONLY
         */
            AppActivityPeriodicTaskReceiver.createAlarms(getApplicationContext());


        /*
        *
        * END DEBUG CODE ONLY
         */

//        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//
//        List<ActivityManager.RunningAppProcessInfo> info = activityManager.getRunningAppProcesses();
//        for(ActivityManager.RunningAppProcessInfo i : info){
//            Log.d("Appspy-2", i.processName);
//        }

    }
}
