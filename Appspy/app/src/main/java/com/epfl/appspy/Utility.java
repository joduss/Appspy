package com.epfl.appspy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.epfl.appspy.ApplicationsInformation;
import com.epfl.appspy.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Jonathan Duss on 11.04.15.
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
        String context_usage_stats_service = "usagestats"; // = Context.USAGE_STATS_SERVICE, but this is not recognize for an unknown reason
        @SuppressWarnings("ResourceType") UsageStatsManager manager = (UsageStatsManager) context.getSystemService(context_usage_stats_service);


        List<UsageStats> statistics =
                manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0,
                                        System.currentTimeMillis());

        LogA.d("Appspy", "usageStats granted? " + (statistics != null && statistics.size() > 0));
        
        return statistics != null && statistics.size() > 0;
    }


    private static Process p;


    /**
     * Start to write log in a file
     */
    public static void startLogging(){
        try {
            File path = Environment.getExternalStorageDirectory();
            //if folver /tmp/appspy does not exists, create it
            File appspyTmpFolder = new File(path + "/" + GlobalConstant.APPSPY_TMP_DIR);
            appspyTmpFolder.mkdirs();

            if(p != null){
                p.destroy();
            }
            p = Runtime.getRuntime().exec("logcat -v time -f " + appspyTmpFolder + "/" + GlobalConstant.LOG_FILENAME + " *:I " + " SQLiteLog:S");

            LogA.i("Appspy-AppActivityTracker","Start logging in file " + appspyTmpFolder + "/" + GlobalConstant.LOG_FILENAME );

        } catch (IOException e) {
            e.printStackTrace();
            LogA.i("Appspy-AppActivityTracker","Failed to start logging" );

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

}




