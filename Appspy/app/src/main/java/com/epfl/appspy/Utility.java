package com.epfl.appspy;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Jonathan Duss on 11.04.15.
 *
 * Utility calss
 */
public class Utility {


    //A solution given on. Taking all the record over the previous year apparently does not always work
    //http://stackoverflow.com/questions/27215013/check-if-my-application-has-usage-access-enabled

    /**
     * Say if the usageStats permission is granted to our application
     * @param context
     * @return if usage stats are granted to our app
     */
    public static boolean usageStatsPermissionGranted(Context context){
        @SuppressWarnings("ResourceType")

        AppOpsManager appOps = (AppOpsManager) context
                .getSystemService(Context.APP_OPS_SERVICE);
        boolean granted = appOps.checkOpNoThrow("android:get_usage_stats",
                                                android.os.Process.myUid(), context.getPackageName()) == AppOpsManager.MODE_ALLOWED;


        return granted;
    }


    private static Process p;


    /**
     * Start to write log in a file
     */
    public static void startLogging(){
        try {

            boolean running = true;
            try {
                //check if p has been init already. If the case, check if still runing
                if (p != null) {
                    p.exitValue(); // return something is thread has terminated
                    running = false;
                }
                //thread is not running for the log. Start it now
                File path = Environment.getExternalStorageDirectory();
                //if folver /tmp/appspy does not exists, create it
                File appspyTmpFolder = new File(path + "/" + GlobalConstant.APPSPY_TMP_DIR);
                appspyTmpFolder.mkdirs();


                p = Runtime.getRuntime().exec(
                        "logcat -v time -f " + appspyTmpFolder + "/" + GlobalConstant.LOG_FILENAME + " *:d " +
                        " SQLiteLog:S");


                LogA.d("Appspy-AppActivityTracker","Start logging in file " + appspyTmpFolder + "/" + GlobalConstant.LOG_FILENAME );


            } catch(IllegalThreadStateException e){
                //mean it is still running
                // do nothing
                LogA.d("Appspy-AppActivityTracker","Logging process is still running" );
            }




        } catch (IOException e) {
            e.printStackTrace();
            LogA.i("Appspy-AppActivityTracker","Failed to start logging" );

        }
    }


    /**
     * Copy files from sourceFile to destinationFile
     * @param sourceFile
     * @param destFile
     * @throws IOException
     */
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
    public static void zipFolder(String inputFolderPath, String outZipPath) {
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


    /**
     * Remove a folder
     * @param folder the folder to remove
     */
    public static void deleteFolder(File folder){
        if(folder.isDirectory()){
            //firts remove all the files that are in the folder
            for(File f : folder.listFiles()){
                LogA.d("Appspy","delete");
                if(f.isDirectory()){
                    deleteFolder(f);
                }
                else {
                    LogA.d("Appspy","delete the file");
                    f.delete();
                }
            }

            //finally, delete it
            LogA.d("Appspy","final deletion");
            folder.delete();
        }
        else {
            LogA.d("Appspy","is file");
            folder.delete();
        }
    }


    /**
     * Format date into a beautiful representation
     * @param millis
     * @return
     */
    public static String beautifulDate(long millis){
        Calendar d = Calendar.getInstance();
        d.setTimeInMillis(millis);

        String hour = "" + d.get(Calendar.HOUR_OF_DAY);
        String min = "" + d.get(Calendar.MINUTE);
        String sec = "" + d.get(Calendar.SECOND);

        return hour + ":" + min + ":" + sec;
    }


    /**
     * Return the day of the given millis time
     * @param millis
     * @return
     */
    public static int getDay(long millis){
        Calendar d = Calendar.getInstance();
        d.setTimeInMillis(millis);

        return d.get(Calendar.DAY_OF_YEAR);
    }

}




