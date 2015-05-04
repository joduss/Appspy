package com.epfl.appspy.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.epfl.appspy.LogA;
import com.epfl.appspy.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epfl.appspy.database.DatabaseNames.COL_ACCURACY;
import static com.epfl.appspy.database.DatabaseNames.COL_ALTITUDE;
import static com.epfl.appspy.database.DatabaseNames.COL_APP_ID;
import static com.epfl.appspy.database.DatabaseNames.COL_APP_NAME;
import static com.epfl.appspy.database.DatabaseNames.COL_APP_PKG_NAME;
import static com.epfl.appspy.database.DatabaseNames.COL_AVG_CPU_USAGE;
import static com.epfl.appspy.database.DatabaseNames.COL_BOOT;
import static com.epfl.appspy.database.DatabaseNames.COL_DOWNLOADED_DATA;
import static com.epfl.appspy.database.DatabaseNames.COL_FOREGROUND_TIME_USAGE;
import static com.epfl.appspy.database.DatabaseNames.COL_GPS_TYPE;
import static com.epfl.appspy.database.DatabaseNames.COL_INSTALLATION_DATE;
import static com.epfl.appspy.database.DatabaseNames.COL_IS_SYSTEM;
import static com.epfl.appspy.database.DatabaseNames.COL_LAST_TIME_USE;
import static com.epfl.appspy.database.DatabaseNames.COL_LATITUDE;
import static com.epfl.appspy.database.DatabaseNames.COL_LONGITUDE;
import static com.epfl.appspy.database.DatabaseNames.COL_MAX_CPU_USAGE;
import static com.epfl.appspy.database.DatabaseNames.COL_PERMISSION_GAIN_ACCESS;
import static com.epfl.appspy.database.DatabaseNames.COL_PERMISSION_LOST_ACCESS;
import static com.epfl.appspy.database.DatabaseNames.COL_PERMISSION_NAME;
import static com.epfl.appspy.database.DatabaseNames.COL_RECORD_ID;
import static com.epfl.appspy.database.DatabaseNames.COL_RECORD_TIME;
import static com.epfl.appspy.database.DatabaseNames.COL_UNINSTALLATION_DATE;
import static com.epfl.appspy.database.DatabaseNames.COL_UPLOADED_DATA;
import static com.epfl.appspy.database.DatabaseNames.COL_WAS_FOREGROUND;
import static com.epfl.appspy.database.DatabaseNames.CREATE_TABLE_APPS_ACTIVITY;
import static com.epfl.appspy.database.DatabaseNames.CREATE_TABLE_APPS_ACTIVITY_LAST_TIME;
import static com.epfl.appspy.database.DatabaseNames.CREATE_TABLE_APPS_INTERNET_USE_LAST_TIME;
import static com.epfl.appspy.database.DatabaseNames.CREATE_TABLE_GPS_LOCATION;
import static com.epfl.appspy.database.DatabaseNames.CREATE_TABLE_INSTALLED_APPS;
import static com.epfl.appspy.database.DatabaseNames.CREATE_TABLE_PERMISSIONS;
import static com.epfl.appspy.database.DatabaseNames.CREATE_VIEW_APP_ACTIVITY;
import static com.epfl.appspy.database.DatabaseNames.CREATE_VIEW_GPS;
import static com.epfl.appspy.database.DatabaseNames.CREATE_VIEW_INSTALLED_APPS;
import static com.epfl.appspy.database.DatabaseNames.CREATE_VIEW_PERMISSIONS;
import static com.epfl.appspy.database.DatabaseNames.DB_NAME;
import static com.epfl.appspy.database.DatabaseNames.DB_VERSION;
import static com.epfl.appspy.database.DatabaseNames.TABLE_APPS_ACTIVITY;
import static com.epfl.appspy.database.DatabaseNames.TABLE_APPS_ACTIVITY_LAST_TIME;
import static com.epfl.appspy.database.DatabaseNames.TABLE_APPS_INTERNET_USE_LAST_TIME;
import static com.epfl.appspy.database.DatabaseNames.TABLE_GPS_LOCATION;
import static com.epfl.appspy.database.DatabaseNames.TABLE_INSTALLED_APPS;
import static com.epfl.appspy.database.DatabaseNames.TABLE_PERMISSIONS;
import static com.epfl.appspy.database.DatabaseNames.VIEW_APP_ACTIVITY;
import static com.epfl.appspy.database.DatabaseNames.VIEW_GPS;
import static com.epfl.appspy.database.DatabaseNames.VIEW_INSTALLED_APPS;
import static com.epfl.appspy.database.DatabaseNames.VIEW_PERMISSIONS;

/**
 * Manage the database of the app
 * <p/>
 * There is 1 table: - TABLE_APPS_ACTIVITY, stores timestamps, where the app was active (background, or foreground)
 * <p/>
 * Created by Jonathan Duss on 22.02.15.
 */
public class Database extends SQLiteOpenHelper {


    private static Database databaseInstance;

    public static synchronized Database getDatabaseInstance(Context context){
        if(databaseInstance == null){
            databaseInstance = new Database(context);
        }
        return databaseInstance;
    }

    private Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        createDB(db);
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_INSTALLED_APPS);
        db.execSQL("DROP TABLE " + TABLE_APPS_ACTIVITY);
        db.execSQL("DROP TABLE " + TABLE_PERMISSIONS);
        db.execSQL("DROP TABLE " + TABLE_APPS_INTERNET_USE_LAST_TIME);
        db.execSQL("DROP TABLE " + TABLE_GPS_LOCATION);
        db.execSQL("DROP TABLE " + TABLE_APPS_ACTIVITY_LAST_TIME);


        db.execSQL("DROP VIEW " + VIEW_GPS);
        db.execSQL("DROP VIEW " + VIEW_PERMISSIONS);
        db.execSQL("DROP VIEW " + VIEW_INSTALLED_APPS);
        db.execSQL("DROP VIEW " + VIEW_APP_ACTIVITY);

        createDB(db);
    }

    private void createDB(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE_APPS_ACTIVITY);
        db.execSQL(CREATE_TABLE_INSTALLED_APPS);
        db.execSQL(CREATE_TABLE_PERMISSIONS);
        db.execSQL(CREATE_TABLE_APPS_INTERNET_USE_LAST_TIME);
        db.execSQL(CREATE_TABLE_GPS_LOCATION);
        db.execSQL(CREATE_TABLE_APPS_ACTIVITY_LAST_TIME);

        db.execSQL(CREATE_VIEW_APP_ACTIVITY);
        db.execSQL(CREATE_VIEW_GPS);
        db.execSQL(CREATE_VIEW_INSTALLED_APPS);
        db.execSQL(CREATE_VIEW_PERMISSIONS);
    }

    public void deviceStarted(){
        this.getWritableDatabase().execSQL("DROP TABLE " + TABLE_APPS_INTERNET_USE_LAST_TIME);
        this.getWritableDatabase().execSQL(CREATE_TABLE_APPS_INTERNET_USE_LAST_TIME);
    }


    /**
     * @param packageInfo
     * @return
     * @throws Resources.NotFoundException the database of installed apps contains no information about this package
     */
    public int getAppIdForPackage(PackageInfo packageInfo) throws Resources.NotFoundException {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + COL_APP_ID + "," + COL_APP_PKG_NAME + " FROM " + TABLE_INSTALLED_APPS;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int appId = cursor.getInt(cursor.getColumnIndex(COL_APP_ID));
                String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));

                if (pkgName.equals(packageInfo.packageName)) {
                    return appId;
                }
            } while (cursor.moveToNext());
        }

        //If nothing found, that means there is an error
        throw new Resources.NotFoundException();
    }


    /**
     * Returns the id (called appID) associated with that packageName in the table of installed apps
     * @param packageName the package name for which the appId is requested
     * @return appId
     */
    public int getAppId(String packageName) {
        SQLiteDatabase db = getReadableDatabase();
        String query =
                "SELECT " + COL_APP_ID + " FROM " + TABLE_INSTALLED_APPS + " WHERE " + COL_APP_PKG_NAME + "=" + "\"" +
                packageName + "\"";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst() && cursor.getCount() == 1) {
            return cursor.getInt(cursor.getColumnIndex(COL_APP_ID));

        } else {
            return -1;
        }
    }

    //##################################################################################################################
    //##################################################################################################################
    // TABLE APPLICATION INSTALLED APPS
    //##################################################################################################################
    //##################################################################################################################

    /**
     * Return true if an app exist on the phone and hasn't been uninstalled
     * Return false
     * @param packageName name of the package of app
     * @return if there is a record for that app
     */
    public boolean installationRecordExists(String packageName) {
        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT " + COL_APP_PKG_NAME + " FROM " + TABLE_INSTALLED_APPS + " WHERE " + COL_APP_PKG_NAME + " =\"" +
                packageName + "\""  + " AND " + COL_UNINSTALLATION_DATE + "=0" ;;

        Cursor cursor = db.rawQuery(query, null);

        return cursor.moveToFirst();
    }

    /**
     * Add an installation record about an app in the database if there is none, or update it if it already exists. The
     * existence is determined based on the package name.
     *
     * @param newRecord the new record to add, or to update if the package is already stored
     */
    public void addOrUpdateApplicationInstallationRecord(ApplicationInstallationRecord newRecord) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //id will be created, none exist for the new record


        //If the package_name does not exist, we add a new record
        if (installationRecordExists(newRecord.getPackageName()) == false) {
            LogA.i("Appspy-DB", "A new ApplicationInstallationRecord has been added: " + newRecord.getApplicationName());
            values.put(COL_APP_NAME, newRecord.getApplicationName());
            values.put(COL_APP_PKG_NAME, newRecord.getPackageName());
            values.put(COL_INSTALLATION_DATE, newRecord.getInstallationDate());
            values.put(COL_UNINSTALLATION_DATE, newRecord.getUninstallationDate());
            values.put(COL_IS_SYSTEM, newRecord.isSystem());

            db.insert(TABLE_INSTALLED_APPS, null, values);

        }
        //if record exists and was uninstalled (uninstallationDate > 0), update the uninstallationDate
        else if(newRecord.getUninstallationDate() > 0){
            //If it exists, as package_name is a unique identifier of an app, it means, there is already a record about it.
            LogA.i("Appspy-DB", "one applicationInstallationRecord has been updated for uninstall: " + newRecord.getApplicationName());


            //newRecord contains the updated values
            //oldRecord contains the one already in the DB
            ApplicationInstallationRecord oldRecord = getApplicationInstallationRecord(newRecord.getPackageName());


            //update only a few columns. The other stay the same.
            values.put(COL_APP_ID, oldRecord.getAppId());
            values.put(COL_APP_NAME, oldRecord.getApplicationName());
            values.put(COL_APP_PKG_NAME, oldRecord.getPackageName());
            values.put(COL_INSTALLATION_DATE, oldRecord.getInstallationDate());
            values.put(COL_UNINSTALLATION_DATE, newRecord.getUninstallationDate()); //updated with new
            values.put(COL_IS_SYSTEM, oldRecord.isSystem());



            //SQL query. Update the row for the current packageName
            db.update(TABLE_INSTALLED_APPS, values, COL_APP_ID + "=" + oldRecord.getAppId(), null);
        }
        //else if exists, but was not uninstalled ( uninstallationDate = 0), do nothing, no need to update

    }


//    public List<ApplicationInstallationRecord> getAllApplicationInstallationRecords() {
//        //TODO implement for real
////        SQLiteDatabase db = getReadableDatabase();
////
////        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_WAS_BACKGROUND + " = 0 ";
////
////        Cursor cursor = db.rawQuery(query, null);
////
////
////        if (cursor.moveToFirst()) {
////            do {
////                Log.d("Appspy", "IS IN DB:" + cursor.getString(cursor.getColumnIndex(COL_APP_NAME)));
////            } while (cursor.moveToNext());
////        }
//        return null;
//    }


    /**
     * Returns a list of packageName of all apps that were currently installed on the device
     * @return List of packagesName installed in the last record
     */
    public List<String> getInstalledAppsPackageNameInLastRecord(){
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + COL_APP_PKG_NAME + " FROM " + TABLE_INSTALLED_APPS + " WHERE " + COL_UNINSTALLATION_DATE + " = 0 ";

        Cursor cursor = db.rawQuery(query, null);

        ArrayList<String> packages = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String packageName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
                packages.add(packageName);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return packages;
    }


    /**
     * Get the  ApplicationInstallationRecord about the application whose package name is provided in argument
     * If the same app was installed, then uninstalled, then it will return null
     * If it has been reinstall, if returns the record of the last time the app was reinstalled (if not removed again,
     * in which case is returns null)
     *
     * @param packageName the package name of the record wanted
     * @return the ApplicationInstallationRecord associated to the packageName
     */
    public ApplicationInstallationRecord getApplicationInstallationRecord(String packageName) {
        SQLiteDatabase db = getReadableDatabase();
        String query =
                "SELECT * FROM " + TABLE_INSTALLED_APPS + " WHERE " + COL_APP_PKG_NAME + "=" + "\"" + packageName +
                "\" " + " AND " + COL_UNINSTALLATION_DATE + "=0" ;
        Cursor cursor = db.rawQuery(query, null);

        //Verify that the is exactly one record. As the package name is unique, there is 0 or 1 row in the cursor.
        //if 0, moveToFirst returns false
        if (cursor.moveToFirst()) {

            ApplicationInstallationRecord record = cursorToAppInstallationRecord(cursor);

            cursor.close();
            return record;
        } else {
            //As packageName is unique, there is anyway at maximum one. The other case is
            //therefore when no record is found. In such case, we return null
            cursor.close();
            return null;
        }
    }


    /**
     * return a list of ApplicationInstallationRecord for app that were installed (present) on the devices in the
     * give time range.
     * @param begin
     * @param end
     * @return
     */
    public Set<ApplicationInstallationRecord> getApplicationInstalledTimeRange(long begin, long end){
        SQLiteDatabase db = getReadableDatabase();
        String query =
                "SELECT * FROM " + TABLE_INSTALLED_APPS + " WHERE " + COL_INSTALLATION_DATE + "<" + end + " AND " + COL_UNINSTALLATION_DATE + ">" + begin + " AND " + COL_UNINSTALLATION_DATE + "!=0" ;
        Cursor cursor = db.rawQuery(query, null);

        HashSet<ApplicationInstallationRecord> apps = new HashSet<>();


        if (cursor.moveToFirst()) {
            apps.add(cursorToAppInstallationRecord(cursor));
        }

        cursor.close();
        return apps;
    }

    public Set<ApplicationInstallationRecord> getAllTimeApplicationInstalled(){
        SQLiteDatabase db = getReadableDatabase();
        String query =
                "SELECT * FROM " + TABLE_INSTALLED_APPS ;
        Cursor cursor = db.rawQuery(query, null);

        HashSet<ApplicationInstallationRecord> apps = new HashSet<>();


        if (cursor.moveToFirst()) {
            apps.add(cursorToAppInstallationRecord(cursor));
        }

        cursor.close();
        return apps;
    }


    /**
     *
     * @return
     */
    public Cursor getAllTimeApplicationInstalledCursor(){
        SQLiteDatabase db = getReadableDatabase();
        String query =
                "SELECT " + COL_APP_ID + " as _id, * FROM " + TABLE_INSTALLED_APPS + " ORDER BY " + COL_APP_NAME ;
        Cursor cursor = db.rawQuery(query, null);

        return cursor;
    }


    /**
     * If cursor contains all field and is being iterate: create a AppInstallationRecord from it (only the current iteration)
     * @param cursor
     * @return
     */
    private ApplicationInstallationRecord cursorToAppInstallationRecord(Cursor cursor){
        int appId = cursor.getInt(cursor.getColumnIndex(COL_APP_ID));
        String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
        String appName = cursor.getString(cursor.getColumnIndex(COL_APP_NAME));
        long installationDate = cursor.getLong(cursor.getColumnIndex(COL_INSTALLATION_DATE));
        long uninstallationDate = cursor.getLong(cursor.getColumnIndex(COL_UNINSTALLATION_DATE));
        boolean appIsSystem = cursor.getInt(cursor.getColumnIndex(COL_IS_SYSTEM)) == 1;

        return new ApplicationInstallationRecord(appId, appName, pkgName, installationDate, uninstallationDate,
                                                  appIsSystem);
    }





    //##################################################################################################################
    //##################################################################################################################
    // TABLE APPLICATION ACTIVITY
    //##################################################################################################################
    //##################################################################################################################


    /**
     * Check the there is already a previous record of activity for that package name today
     * @param packageName PackageName for which it has to be checked
     * @return if such a record exists
     */
    boolean checkIfFirstRecordOfDay(String packageName){
        ApplicationActivityRecord record = getLastApplicationActivityRecord(packageName);

        Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        if(record == null || record.getRecordTime() < c.getTimeInMillis()){
            return true;
        }
        return false;
    }

        /**
         * Add activity record and automatically set if the app was active on foreground. In the othercase, it check
         * if an activity occured on background. Otherwise, no record is added.
         * If previous was considered as not active, it will fill the missing records
         * @param newRecord new record, with down/uploaded data total for that app that day. Computation will be made autmatically
         */
    public void addApplicationActivityRecordIntelligent(ApplicationActivityRecord newRecord) {
        SQLiteDatabase db = this.getWritableDatabase();


        //FIRST: check if there exists a record that exits, without end time for that app
        //then verify that this record starttime is today
        //then update start time

        //of the last_use_time

        ApplicationActivityRecord lastRecord = getLastTemporaryAppActivityRecord(newRecord.getPackageName());//getLastApplicationActivityRecord(newRecord.getPackageName());
        boolean wasForeground;
        boolean wasActiveInBackground;


        long downloadedData = newRecord.getDownloadedData() - getLastTotalDataDownloaded(newRecord.getPackageName());
        long uploadedData = newRecord.getUploadedData() - getLastTotalDataUploaded(newRecord.getPackageName());

        //Check if apps was active on foreground
        if(lastRecord == null){
            //mean the app was newly opened, thus active
            wasForeground = true;
            wasActiveInBackground = false;

            Log.d("Appspy-DB","last records of appactivity: NULL");
        }
        else if(lastRecord.getForegroundTime() != newRecord.getForegroundTime()){
            //foreground time changed, thus app was active on foreground
            wasForeground = true;
            wasActiveInBackground = false;
            LogA.d("Appspy-DB","else if + " + lastRecord.getForegroundTime() + "   " + newRecord.getForegroundTime());
        }
        else {
            //then the app was in background. Need to check if it was active (did down/upload data or used cpu)
            wasForeground = false;

            //check if app was active in background (did downloaded/upload some data, or used cpu)
            if(uploadedData > 0 ||
               downloadedData > 0 || newRecord.getAvgCpuUsage() > 0 ) {
                wasActiveInBackground = true;
            }
            else {
                wasActiveInBackground = false;
            }

        }

        //If is was active in a way, we add the record to the DB
        if(wasForeground || wasActiveInBackground){


            //update the LastInternetUse: to be able to compute how much data were up/down next time
            addLastInternetUse(newRecord.getPackageName(), newRecord.getUploadedData(), newRecord.getDownloadedData());

            /*
            * if the app was used a long time and no stat were fired for a moment, some records may say the app was in
            * background. So need to update correctly these
             */

            if (lastRecord != null && wasForeground){

                final long activeTime = newRecord.getForegroundTime() - lastRecord.getForegroundTime();

                final long beginActivity = newRecord.getLastTimeUsed() - activeTime-1000; //threshold

                //get all record between opening of app and current recognized on FG record.
                //they need to be fixed, because they were recognized wrongly as on BG (background)
                List<ApplicationActivityRecord> records = getAppActivityInTimeRange(beginActivity,
                                                                                    newRecord.getRecordTime(),
                                                                                    newRecord.getPackageName());



                if(records.size() > 0) {
                    /*
                    *  last record was not updated with foreground time in case of long activity without interaction
                    *  (watching a movie for example)
                    *
                    *  So we need to fix the wasForeground field that would be false instead of true
                    *  We need also to fix the ForegroundTime. If the app was used for a 1h movie without screen
                    *  interaction,there would be a record when the movie begin and when the user close the app
                    *  => need to add record inbetween
                    *  But there may be records in case internet was used by the app at the same time. Need to update
                    *  the up/down data for them
                     */

                    //Can't use lastRecord
                    // maybe it is older than 1 minutes in case long continuous use
                    //last record is the last record where app was active (interaction recognized by stat or data usage)
                    //last record FG is the last FG known by stats. But maybe not up-to-date

                    //first compute in the current interval, how long the app was open, because the app was certainly closed
                    long activeTimeInCurrentInterval = newRecord.getLastTimeUsed() - (newRecord.getRecordTime() - 60000);
                    // the app when we record the app at xx, it means it was close at (xx-1):mm. so it was active during mm.

                    //compute how much time the app was used continuously since last record with usage stat
                    long totalContinuousActivityTimeBeforeCurrent = newRecord.getForegroundTime() - lastRecord.getForegroundTime() - activeTimeInCurrentInterval;

                    //sorted by time ascending, invert it to have order in reverse time order
                    Collections.reverse(records);

                    ApplicationActivityRecord lastRecordProcessed = newRecord;

                    /*
                     * Look at all the record (recognized wrongly as on background)
                     * Start with the newest ones. to which need to add more missing foreground time, then remove
                     * each step 60 seconds (as app was used during 60 seconds), until all have been processed
                     */
                    for (ApplicationActivityRecord record : records) {

                        //these were on foreground. Need to update them
                        long timeBetweenRecords = lastRecordProcessed.getRecordTime() - record.getRecordTime();


                        //there is a missing record (time between the record and the previous one too large) => need to add it
                        //add new until interval is ~60000
                        while(timeBetweenRecords > 70000){


                            //last time used is right at time of record, as the use hasn't stopped
                            ApplicationActivityRecord toAdd = new ApplicationActivityRecord(newRecord.getPackageName(), lastRecordProcessed.getRecordTime() - 60000, record.getForegroundTime() + totalContinuousActivityTimeBeforeCurrent, lastRecordProcessed.getRecordTime() - 60000, 0, 0, true, false);
                            simpleAddActivityRecord(toAdd);
                            LogA.d("Appspy-DB","missing added");
                            lastRecordProcessed = toAdd;
                            timeBetweenRecords = lastRecordProcessed.getRecordTime() - record.getRecordTime();

                            totalContinuousActivityTimeBeforeCurrent -= 60000;

                        }
                        //should not be < 0, but because of threshold, it may. So we exclude that case
                        if(totalContinuousActivityTimeBeforeCurrent > 0) {

                            //update the FG time
                            //as the app was used a long time, no update of FG were made. Here we update it.
                            record.setForegroundTime(
                                    record.getForegroundTime() + totalContinuousActivityTimeBeforeCurrent);
                            record.setWasForeground(true);
                            record.setLastTimeUsed(record.getRecordTime()); //last time is right at time of record, as the use hasn't stopped
                            updateApplicationActivityRecord(record);


                            //update lastRecordProcessed. And remove 60 seconds that were "consumed" by going in the past
                            lastRecordProcessed = record;
                            totalContinuousActivityTimeBeforeCurrent -= 60000;
                        }
                        else {
                            LogA.d("Appspy","IS SMALLER");
                        }

                    }
                }
                LogA.d("Appspy-DB", "UPDATE foreground " + newRecord.getPackageName());

                //all these records have been active on foreground
            }

            //fix border effect when booting
            //at boot, every apps are in background if active
            if(checkIfFirstRecordOfDay(newRecord.getPackageName())) {
                wasForeground = false;
            }

            ContentValues values = new ContentValues();
            //id will be created, none exist for the new record
            values.put(COL_APP_PKG_NAME, newRecord.getPackageName());
            values.put(COL_RECORD_TIME, newRecord.getRecordTime());
            values.put(COL_FOREGROUND_TIME_USAGE, newRecord.getForegroundTime());
            values.put(COL_LAST_TIME_USE, newRecord.getLastTimeUsed());
            values.put(COL_DOWNLOADED_DATA, downloadedData);
            values.put(COL_UPLOADED_DATA, uploadedData);
            values.put(COL_AVG_CPU_USAGE, newRecord.getAvgCpuUsage());
            values.put(COL_MAX_CPU_USAGE, newRecord.getMaxCpuUsage());
            values.put(COL_WAS_FOREGROUND, wasForeground);
            values.put(COL_BOOT, newRecord.isBoot());

            long id = db.insert(TABLE_APPS_ACTIVITY, null, values);
            newRecord.setRecordId(id);

            setTemporaryLastAppActivity(newRecord.getPackageName(), newRecord.getRecordTime(),
                                        newRecord.getForegroundTime(), newRecord.getLastTimeUsed());


            LogA.d("Appspy-DB", "New application activity record added for " + newRecord.getPackageName());
        }
    }


    /**
     * Add an activity record in the most simple way possible
     * @param newRecord
     */
    private void simpleAddActivityRecord(ApplicationActivityRecord newRecord){
        ContentValues values = new ContentValues();
        //id will be created, none exist for the new record
        values.put(COL_APP_PKG_NAME, newRecord.getPackageName());
        values.put(COL_RECORD_TIME, newRecord.getRecordTime());
        values.put(COL_FOREGROUND_TIME_USAGE, newRecord.getForegroundTime());
        values.put(COL_LAST_TIME_USE, newRecord.getLastTimeUsed());
        values.put(COL_DOWNLOADED_DATA, newRecord.getDownloadedData());
        values.put(COL_UPLOADED_DATA, newRecord.getUploadedData());
        values.put(COL_AVG_CPU_USAGE, newRecord.getAvgCpuUsage());
        values.put(COL_MAX_CPU_USAGE, newRecord.getMaxCpuUsage());
        values.put(COL_WAS_FOREGROUND, newRecord.isWasForeground());
        values.put(COL_BOOT, newRecord.isBoot());

        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(TABLE_APPS_ACTIVITY, null, values);
        newRecord.setRecordId(id);
    }


    /**
     * Return the most recent record about the application
     * @param packageName the package name of the app
     * @return the most recent record
     */
    public ApplicationActivityRecord getLastApplicationActivityRecord(String packageName){

        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_RECORD_TIME + "=" +
                "(" +
                "SELECT MAX(" + COL_RECORD_TIME + ") FROM " + TABLE_APPS_ACTIVITY +
                " WHERE " + COL_APP_PKG_NAME + "=\"" + packageName + "\"" +
                ")" +
                " AND " + COL_APP_PKG_NAME + "=\"" + packageName + "\"";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery(query, null);

        if (result.moveToFirst()) {
            do {
                long recordID = result.getLong(result.getColumnIndex(COL_RECORD_ID));
                long recordTime = result.getLong(result.getColumnIndex(COL_RECORD_TIME));
                long foregroundTime = result.getLong(result.getColumnIndex(COL_FOREGROUND_TIME_USAGE));
                long lastUsed = result.getLong(result.getColumnIndex(COL_LAST_TIME_USE));
                long downloaded = result.getLong(result.getColumnIndex(COL_DOWNLOADED_DATA));
                long uploaded = result.getLong(result.getColumnIndex(COL_UPLOADED_DATA));
                double avgCpuUsage = result.getDouble(result.getColumnIndex(COL_AVG_CPU_USAGE));
                int maxCpuUsage = result.getInt(result.getColumnIndex(COL_MAX_CPU_USAGE));
                boolean wasForeground = result.getInt(result.getColumnIndex(COL_WAS_FOREGROUND)) == 1;
                boolean boot = result.getInt(result.getColumnIndex(COL_BOOT)) == 1;

                result.close();
                return new ApplicationActivityRecord(recordID,packageName,recordTime, foregroundTime, lastUsed,
                                                     uploaded, downloaded, avgCpuUsage, maxCpuUsage, wasForeground, boot);


            } while (result.moveToNext());
        }
        return null;
    }


    /**
     * Stores the last activity for data usage. Will be used to compute the dif between each minute of activity
     * @param packageName
     * @param uploadedData
     * @param downloadedData
     */
    public void setLastDataUsageActivity(String packageName, long uploadedData, long downloadedData){
        addLastInternetUse(packageName, uploadedData, downloadedData);
    }


    /**
     * Add the usage stats for that app in the temporary DB. It will be used to compare with the next future record, to
     * compute the usage over a time interval
     * @param packageName
     * @param recordTime
     * @param foregroundTime
     * @param lastTimeUsed
     */
    private void setTemporaryLastAppActivity(String packageName, long recordTime, long foregroundTime,
                                             long lastTimeUsed){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY_LAST_TIME +
                       " WHERE " + COL_APP_PKG_NAME + "=\"" + packageName  +"\"";

        Cursor result = db.rawQuery(query, null);

        LogA.d("Appspy-DB","setTemporaryLastAppActivity");

        if (result.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(COL_RECORD_ID, result.getLong(result.getColumnIndex(COL_RECORD_ID)));
            values.put(COL_RECORD_TIME, recordTime);
            values.put(COL_APP_PKG_NAME, packageName);
            values.put(COL_FOREGROUND_TIME_USAGE, foregroundTime);
            values.put(COL_LAST_TIME_USE, lastTimeUsed);
            LogA.d("Appspy-DB","setTemporaryLastAppActivity - update");

            db.update(TABLE_APPS_ACTIVITY_LAST_TIME, values, COL_APP_PKG_NAME + "=\"" + packageName + "\"", null);
        }
        else{
            ContentValues values = new ContentValues();
            values.put(COL_APP_PKG_NAME, packageName);
            values.put(COL_RECORD_TIME, recordTime);
            values.put(COL_FOREGROUND_TIME_USAGE, foregroundTime);
            values.put(COL_LAST_TIME_USE, lastTimeUsed);
            LogA.d("Appspy-DB","setTemporaryLastAppActivity - insert");

            db.insert(TABLE_APPS_ACTIVITY_LAST_TIME, null, values);
        }

        result.close();
    }

//    /**
//     * Return the most recent record older than the one given in parameter
//     * @param record the record to compare with
//     * @return
//     */
//    public ApplicationActivityRecord getPreviousApplicationActivityRecord(ApplicationActivityRecord record){
//
//        final String packageName = record.getPackageName();
//
//        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_RECORD_TIME + "=" +
//                       "(" +
//                       "SELECT MAX(" + COL_RECORD_TIME + ") FROM " + TABLE_APPS_ACTIVITY +
//                       " WHERE " + COL_APP_PKG_NAME + "=\"" + packageName + "\"" +
//                       " AND " + COL_RECORD_TIME + "<" + record.getRecordTime() +
//                       ")" +
//                       " AND " + COL_APP_PKG_NAME + "=\"" + packageName + "\"";
//
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor result = db.rawQuery(query, null);
//
//        if (result.moveToFirst()) {
//            do {
//                long recordID = result.getLong(result.getColumnIndex(COL_RECORD_ID));
//                long recordTime = result.getLong(result.getColumnIndex(COL_RECORD_TIME));
//                long foregroundTime = result.getLong(result.getColumnIndex(COL_FOREGROUND_TIME_USAGE));
//                long lastUsed = result.getLong(result.getColumnIndex(COL_LAST_TIME_USE));
//                long downloaded = result.getLong(result.getColumnIndex(COL_DOWNLOADED_DATA));
//                long uploaded = result.getLong(result.getColumnIndex(COL_UPLOADED_DATA));
//                double avgCpuUsage = result.getDouble(result.getColumnIndex(COL_AVG_CPU_USAGE));
//                int maxCpuUsage = result.getInt(result.getColumnIndex(COL_MAX_CPU_USAGE));
//                boolean wasForeground = result.getInt(result.getColumnIndex(COL_WAS_FOREGROUND)) == 1;
//                boolean boot = result.getInt(result.getColumnIndex(COL_BOOT)) == 1;
//
//                result.close();
//                return new ApplicationActivityRecord(recordID,packageName,recordTime, foregroundTime, lastUsed,
//                                                     uploaded, downloaded, avgCpuUsage, maxCpuUsage, wasForeground, boot);
//
//            } while (result.moveToNext());
//        }
//        return null;
//    }


    /**
     * Add (if none) or update the previous stats about total data uploaded/downloaded since boot for that app
     * (used to compare and compute how much data have been down/up since last time there was a record
     * @param packageName packageName about which stats are wanted
     * @param totalUploadedData totalUploadedData now
     * @param totalDownloadedData totalDownloadedData now
     */
    private void addLastInternetUse(String packageName, long totalUploadedData, long totalDownloadedData) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_APPS_INTERNET_USE_LAST_TIME +
                       " WHERE " + COL_APP_PKG_NAME + "=\"" + packageName  +"\"";

        Cursor result = db.rawQuery(query, null);

        if (result.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(COL_RECORD_ID, result.getLong(result.getColumnIndex(COL_RECORD_ID)));
            values.put(COL_APP_PKG_NAME, result.getString(result.getColumnIndex(COL_APP_PKG_NAME)));
            values.put(COL_DOWNLOADED_DATA, totalDownloadedData);
            values.put(COL_UPLOADED_DATA, totalUploadedData);

            db.update(TABLE_APPS_INTERNET_USE_LAST_TIME, values, COL_APP_PKG_NAME + "=\"" + packageName + "\"", null);
        }
        else{
            ContentValues values = new ContentValues();
            values.put(COL_APP_PKG_NAME, packageName);
            values.put(COL_DOWNLOADED_DATA, totalDownloadedData);
            values.put(COL_UPLOADED_DATA, totalUploadedData);

            db.insert(TABLE_APPS_INTERNET_USE_LAST_TIME, null, values);
        }

        result.close();
    }


    /**
     * Return the total data downloaded by the app for the last record
     * @param packageName packageName for which the total downloadedData is requested
     * @return total downloaded data in byte for that packageName
     */
    private long getLastTotalDataDownloaded(String packageName){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_DOWNLOADED_DATA + " FROM " + TABLE_APPS_INTERNET_USE_LAST_TIME +
                       " WHERE " + COL_APP_PKG_NAME + "=\"" + packageName  +"\"";

        Cursor result = db.rawQuery(query, null);
        long downloaded = 0;
        if (result.moveToFirst()) {
            downloaded = result.getLong(result.getColumnIndex(COL_DOWNLOADED_DATA));
        }

        result.close();
        return downloaded;
    }

    /**
     * Return the total data uploaded by the app for the last record
     * @param packageName packageName for which the total uploadedData is requested
     * @return total uploaded data in byte for that packageName
     */
    private long getLastTotalDataUploaded(String packageName){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_UPLOADED_DATA + " FROM " + TABLE_APPS_INTERNET_USE_LAST_TIME +
                       " WHERE " + COL_APP_PKG_NAME + "=\"" + packageName  +"\"";

        Cursor result = db.rawQuery(query, null);
        long uploaded = 0;
        if (result.moveToFirst()) {
            uploaded = result.getLong(result.getColumnIndex(COL_UPLOADED_DATA));
        }
        result.close();
        return uploaded;
    }


    /**
     * Return the temporary last record that was used and that can be used to compare the foreground time usage
     * If day has changed but phone not reboot, return the last one
     * If the day has change and phone has reboot after it, return null
     * @param packageName
     * @return
     */
    private ApplicationActivityRecord getLastTemporaryAppActivityRecord(String packageName){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *" + " FROM " + TABLE_APPS_ACTIVITY_LAST_TIME +
                       " WHERE " + COL_APP_PKG_NAME + "=\"" + packageName  +"\"";

        Cursor result = db.rawQuery(query, null);

        ApplicationActivityRecord toReturn = null;

        if (result.moveToFirst()) {
            long recordTime = result.getLong(result.getColumnIndex(COL_RECORD_TIME));
            long bootTime = java.lang.System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime();

            if(Utility.getDay(recordTime) == Utility.getDay(System.currentTimeMillis()) && recordTime > bootTime){
                long foregroundTime = result.getLong(result.getColumnIndex(COL_FOREGROUND_TIME_USAGE));
                long lastTimeUsed = result.getLong(result.getColumnIndex(COL_LAST_TIME_USE));
                toReturn = new ApplicationActivityRecord(packageName,recordTime,foregroundTime,lastTimeUsed,0,0, false);
            }
            else {
                LogA.d("Appspy","Record was from yesterday and there was a boot inbetween");
            }
        }
        result.close();
        return toReturn;
    }


    public void updateApplicationActivityRecord(ApplicationActivityRecord record){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //id will be created, none exist for the new record
        values.put(COL_RECORD_ID, record.getRecordId());
        values.put(COL_APP_PKG_NAME, record.getPackageName());
        values.put(COL_RECORD_TIME, record.getRecordTime());
        values.put(COL_FOREGROUND_TIME_USAGE, record.getForegroundTime());
        values.put(COL_LAST_TIME_USE, record.getLastTimeUsed());
        values.put(COL_DOWNLOADED_DATA, record.getDownloadedData());
        values.put(COL_UPLOADED_DATA, record.getUploadedData());
        values.put(COL_AVG_CPU_USAGE, record.getAvgCpuUsage());
        values.put(COL_MAX_CPU_USAGE, record.getMaxCpuUsage());
        values.put(COL_WAS_FOREGROUND, record.isWasForeground());
        values.put(COL_BOOT, record.isBoot());

        db.update(TABLE_APPS_ACTIVITY, values, COL_RECORD_ID + "=" + record.getRecordId(), null);
    }


    /**
     * Return a list that is ordered by time, in ascending order
     * @param begining
     * @param end
     * @param packageName
     * @return
     */
    public List<ApplicationActivityRecord> getAppActivityInTimeRange(long begining, long end, String packageName){

        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_RECORD_TIME + ">=" + begining +
                " AND " + COL_RECORD_TIME + "<" + end + " AND " + COL_APP_PKG_NAME + "=\"" + packageName + "\""
                + " ORDER BY " + COL_RECORD_TIME + " ASC ";

        LogA.d("Appspy", "query:" + query);

        Cursor result = db.rawQuery(query, null);
        //result are ordered descending

        ArrayList<ApplicationActivityRecord> records = new ArrayList<>();

        if(result.moveToFirst()){
            do{
                ApplicationActivityRecord newRec = cursorStartQueryToApplicationActivityRecord(result);
                records.add(newRec);
            } while(result.moveToNext());
        }

        result.close();
        return records;
    }


    /**
     * Return a list that is ordered by time, in ascending order
     * @param begining
     * @param end
     * @param packageName
     * @return
     */
    public List<ApplicationActivityRecord> getAppActivityInTimeRange(long begining, long end, String packageName, boolean foreground){

        SQLiteDatabase db = this.getReadableDatabase();

        int foregroundValue = foreground? 1:0;

        String query =
                "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_RECORD_TIME + ">=" + begining +
                " AND " + COL_RECORD_TIME + "<" + end + " AND " + COL_APP_PKG_NAME + "=\"" + packageName + "\""
                + " AND " + COL_WAS_FOREGROUND + "=" + foregroundValue + " ORDER BY " + COL_RECORD_TIME + " ASC ";

        LogA.d("Appspy", "query:" + query);

        Cursor result = db.rawQuery(query, null);
        //result are ordered descending

        ArrayList<ApplicationActivityRecord> records = new ArrayList<>();

        if(result.moveToFirst()){
            do{
                ApplicationActivityRecord newRec = cursorStartQueryToApplicationActivityRecord(result);
                records.add(newRec);
            } while(result.moveToNext());
        }

        result.close();
        return records;
    }

    private ApplicationActivityRecord cursorStartQueryToApplicationActivityRecord(Cursor result){
        long recordID = result.getLong(result.getColumnIndex(COL_RECORD_ID));
        long recordTime = result.getLong(result.getColumnIndex(COL_RECORD_TIME));
        String packageName = result.getString(result.getColumnIndex(COL_APP_PKG_NAME));
        long foregroundTime = result.getLong(result.getColumnIndex(COL_FOREGROUND_TIME_USAGE));
        long lastUsed = result.getLong(result.getColumnIndex(COL_LAST_TIME_USE));
        long downloaded = result.getLong(result.getColumnIndex(COL_DOWNLOADED_DATA));
        long uploaded = result.getLong(result.getColumnIndex(COL_UPLOADED_DATA));
        double avgCpuUsage = result.getDouble(result.getColumnIndex(COL_AVG_CPU_USAGE));
        int maxCpuUsage = result.getInt(result.getColumnIndex(COL_MAX_CPU_USAGE));
        boolean wasForeground = result.getInt(result.getColumnIndex(COL_WAS_FOREGROUND)) == 1;
        boolean boot = result.getInt(result.getColumnIndex(COL_BOOT)) == 1;

        return new ApplicationActivityRecord(recordID, packageName, recordTime, foregroundTime, lastUsed,
                                             uploaded, downloaded, wasForeground, boot);
    }




    //##################################################################################################################
    //##################################################################################################################
    // TABLE PERMISSIONS
    //##################################################################################################################
    //##################################################################################################################
    /**
     * Add or update the permissions records for the app having the packageName provided
     * @param packageName the package name of the app to which the permissions have to be updated
     * @param records Update of the permissions
     */
    public synchronized void updatePermissionRecordsForApp(String packageName, HashMap<String, PermissionRecord> records) {
        SQLiteDatabase db = getWritableDatabase();

        //Query first to get all the permission for an app, which are currently in use (=last time used = 0)
        String query = "SELECT * FROM " + TABLE_PERMISSIONS + " WHERE " + COL_APP_PKG_NAME + "=" + "\"" +
                       packageName + "\"" + " AND " + COL_PERMISSION_LOST_ACCESS + "=0";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {

            //For all the currently used permissions in the DB for that app, we check if they are
            //in the permissions just checked. If they are, it means that they are still in used
            //thus, nothing need to be done. Sp We jst remove them from the hashmap.
            do {
                String permissionName = cursor.getString(cursor.getColumnIndex(COL_PERMISSION_NAME));
                if (records.containsKey(permissionName)) {
                    records.remove(permissionName);
                }
                else {
                    //If they are not in the hashmap, it means they are not used anymore and we update the record
                    //and set the time where the permission has been dropped as NOW.
                    ContentValues values = new ContentValues();
                    values.put(COL_RECORD_ID, cursor.getLong(cursor.getColumnIndex(COL_RECORD_ID)));
                    values.put(COL_APP_PKG_NAME, cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME)));
                    values.put(COL_PERMISSION_NAME, cursor.getString(cursor.getColumnIndex(COL_PERMISSION_NAME)));
                    values.put(COL_PERMISSION_GAIN_ACCESS,
                               cursor.getLong(cursor.getColumnIndex(COL_PERMISSION_GAIN_ACCESS)));
                    values.put(COL_PERMISSION_LOST_ACCESS, System.currentTimeMillis());
                    db.update(TABLE_PERMISSIONS, values,
                              COL_RECORD_ID + "=" + cursor.getLong(cursor.getColumnIndex(COL_RECORD_ID)), null);
                }
            } while (cursor.moveToNext());
        }

        //At this point, the hashmap only contains permissions that should be inserted in the DB
        //New ones, or "old ones" that were not used anymore, but that are used again, thus it is a new record
        for (PermissionRecord record : records.values()) {
            //LogA.d("Appspy-DB-all", "new permission record");
            ContentValues values = new ContentValues();
            values.put(COL_APP_PKG_NAME, record.getPackageName());
            values.put(COL_PERMISSION_NAME, record.getPermissionName());
            values.put(COL_PERMISSION_GAIN_ACCESS, record.getTimestamp());
            values.put(COL_PERMISSION_LOST_ACCESS, 0); //start using permission now. no lastuse so set = 0
            db.insert(TABLE_PERMISSIONS, null, values);
        }

    }

    public void updatePermissionsForUninstalledApp(String packageName){
        updatePermissionRecordsForApp(packageName, new HashMap<String, PermissionRecord>());
    }


    //##################################################################################################################
    //##################################################################################################################
    // TABLE GPS
    //##################################################################################################################
    //##################################################################################################################


    /**
     * Insert a new record for gps location in the Database
     * @param record The record to insert
     */
    public void insertGPSRecord(GPSRecord record){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues toInsert = new ContentValues();
        toInsert.put(COL_RECORD_TIME, record.getRecordTime());
        toInsert.put(COL_LONGITUDE, record.getLongitude());
        toInsert.put(COL_LATITUDE, record.getLatitude());
        toInsert.put(COL_ALTITUDE, record.getAltitude());
        toInsert.put(COL_ACCURACY, record.getAccuracy());
        toInsert.put(COL_GPS_TYPE, record.getLocationType());
        db.insert(TABLE_GPS_LOCATION, null, toInsert);

        LogA.i("Appspy-DB", "new GPS record inserted");
        
    }


}
