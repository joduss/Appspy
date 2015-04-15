package com.epfl.appspy.com.epfl.appspy;

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
import android.provider.Settings;

import com.epfl.appspy.ApplicationsInformation;
import com.epfl.appspy.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

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

        List<UsageStats> statistics =
                manager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, 2000,
                                        year);


        return statistics != null && statistics.size() > 0;
    }


}




