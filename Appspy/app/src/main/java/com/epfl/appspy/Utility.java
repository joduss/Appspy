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

//    //Solution from Stackoverflow
//    //Runtime.exec(cp src dst) => cp is not supported
//    public void copy(File src, File dst) throws IOException {
//        FileInputStream inStream = new FileInputStream(src);
//        FileOutputStream outStream = new FileOutputStream(dst);
//        FileChannel inChannel = inStream.getChannel();
//        FileChannel outChannel = outStream.getChannel();
//        inChannel.transferTo(0, inChannel.size(), outChannel);
//        inStream.close();
//        outStream.close();
//    }

    //Source:
    //http://stackoverflow.com/questions/27215013/check-if-my-application-has-usage-access-enabled
    public static boolean usageStatsPermissionGranted(Context context){
        String context_usage_stats_service = "usagestats"; // = Context.USAGE_STATS_SERVICE, but this is not recognize for an unknown reason
        @SuppressWarnings("ResourceType") UsageStatsManager manager = (UsageStatsManager) context.getSystemService(context_usage_stats_service);


        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        cal.set(year,month,day-1);
        long start = cal.getTimeInMillis();
        cal.set(year,month,day);

        long stop = cal.getTimeInMillis();

        List<UsageStats> statistics =
                manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start,
                                        stop);

        return statistics != null && statistics.size() > 0;
    }


    public static void startLogging(){
        try {
            File path = Environment.getExternalStorageDirectory();
            Runtime.getRuntime().exec("logcat -v time -f " + path.toString() + GlobalConstant.APPSPY_TMP_DIR + "/" + GlobalConstant.LOG_FILENAME + " *:I");
            LogA.i("Appspy-AppActivityTracker","Start logging in file " + path.toString() + GlobalConstant.APPSPY_TMP_DIR + "/" + GlobalConstant.LOG_FILENAME );
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
                if(f.isDirectory()){
                    deleteFolder(f);
                }
                else {
                    f.delete();
                }
            }

            //finally, delete it
            folder.delete();
        }
        else {
            folder.delete();
        }
    }

}




