package com.epfl.appspy;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.epfl.appspy.monitoring.AppActivityTracker;
import com.epfl.appspy.monitoring.GPSTracker;
import com.epfl.appspy.monitoring.InstalledAppsTracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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





    public void sendDB(View v){

        LogA.d("Appspy","click on sendDB");
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

            copyFile(originalDB, copiedDB);


            zipFolder(folderToZip.getAbsolutePath(), zippedFolder.getAbsolutePath());


            Uri uriFileToSend = Uri.parse("file://" + zippedFolder.getAbsolutePath());

            TextView tv = (TextView) findViewById(R.id.pathTextView);
            String text = "";
            text += "PATH is:" + path + "\n";
            text += "PATH exits:" + new File(path).exists() + "\n";

            text += "folder to zip is:"+ folderToZip + "\n";
            text += "zip should exits does it: " + zippedFolder.exists() + "\n";
            text += "zip path is: " + zippedFolder.getAbsolutePath() + "\n";




            tv.setText(text);

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


    //Solution provided on Stackoverflow
    // http://stackoverflow.com/questions/9292954/how-to-make-a-copy-of-a-file-in-android
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    //http://stackoverflow.com/questions/6683600/zip-compress-a-folder-full-of-files-on-android


    /**
     * Zip the content of a folder
     * @param inputFolderPath the folder whose content has to be zipped
     * @param outZipPath the zip file to create
     */
    private static void zipFolder(String inputFolderPath, String outZipPath) {
        try {
            FileOutputStream fos = new FileOutputStream(outZipPath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File srcFile = new File(inputFolderPath);
            File[] files = srcFile.listFiles();
            Log.d("", "Zip directory: " + srcFile.getName());
            for (int i = 0; i < files.length; i++) {
                Log.d("", "Adding file: " + files[i].getName());
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(files[i]);
                zos.putNextEntry(new ZipEntry(files[i].getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
        } catch (IOException ioe) {
            Log.e("", ioe.getMessage());
        }
    }

    public void computeStatNow(View v){

        try {
            File path = Environment.getExternalStorageDirectory();
            Runtime.getRuntime().exec("logcat -v time -f " + path.toString() + GlobalConstant.APPSPY_TMP_DIR + "/" + GlobalConstant.LOG_FILENAME);
            LogA.i("Appspy-AppActivityTracker","Start logging in file " + path.toString() + GlobalConstant.APPSPY_TMP_DIR + "/" + GlobalConstant.LOG_FILENAME );
        } catch (IOException e) {
            e.printStackTrace();
            LogA.i("Appspy-AppActivityTracker","Failed to start logging" );

        }

        Log.d("Appspy","Request to compute stats now");

        //call the InstalledAppsTracker to check all installed apps
        Intent installedAppReceiver = new Intent(getApplicationContext(), InstalledAppsTracker.class);
        installedAppReceiver.setAction(Intent.ACTION_SEND);
        installedAppReceiver.putExtra(GlobalConstant.EXTRA_TAG, GlobalConstant.EXTRA_ACTION.MANUAL);
        sendBroadcast(installedAppReceiver);


        //Launch GPS (useful when app is installed and launched for the first time. After that, not useful
        //the service is started with the boot.
        Intent gpsTaskReceiver = new Intent(getApplicationContext(), GPSTracker.class);
        gpsTaskReceiver.setAction(Intent.ACTION_SEND);
        gpsTaskReceiver.putExtra(GlobalConstant.EXTRA_TAG, GlobalConstant.EXTRA_ACTION.MANUAL);
        sendBroadcast(gpsTaskReceiver);


        Intent activityTaskReceiver = new Intent(getApplicationContext(), AppActivityTracker.class);
        gpsTaskReceiver.setAction(Intent.ACTION_SEND);
        gpsTaskReceiver.putExtra(GlobalConstant.EXTRA_TAG, GlobalConstant.EXTRA_ACTION.MANUAL);
        sendBroadcast(activityTaskReceiver);
    }



    /*
     * Test access to some folder, and show its content.
     * => Try with shell command
     * => Try with shell command as root (only works if device rooted and allows root to apps)
     * => Try using File(path)
     */
    public void testAccess(View v){

        //Get path from user input
        //EditText tv = (EditText) findViewById(R.id.tvPath);
        //String path = tv.getText().toString();


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

}
