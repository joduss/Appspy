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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Manage the database of the app
 * <p/>
 * There is 1 table: - TABLE_APPS_ACTIVITY, stores timestamps, where the app was active (background, or foreground)
 * <p/>
 * Created by Jonathan Duss on 22.02.15.
 */
public class Database extends SQLiteOpenHelper {



    //Database version
    private static final int DB_VERSION = 140;
    private static final String DB_NAME = "Appspy_database";

    //Tables names
    private static final String TABLE_APPS_ACTIVITY = "Table_applications_activity";
    private static final String TABLE_INSTALLED_APPS = "Table_installed_apps";
    private static final String TABLE_PERMISSIONS = "Table_permissions";
    private static final String TABLE_APPS_INTERNET_USE_LAST_TIME = "Table_internet_use_last_time";
    private static final String TABLE_GPS_LOCATION = "Table_GPS_location";

    //SHARED columns names
    private static final String COL_RECORD_ID = "record_id"; //id in any table, except in installed apps
    private static final String COL_APP_NAME = "app_name";
    private static final String COL_APP_PKG_NAME = "package_name";



    //INSTALLED APP TABLE columns names
    private static final String COL_APP_ID = "app_id"; //id in installed app
    private static final String COL_IS_SYSTEM = "is_system";
    private static final String COL_INSTALLATION_DATE = "installation_date";
    private static final String COL_UNINSTALLATION_DATE = "uninstallation_date";

    //PERMISSIONS TABLE columns names
    private static final String COL_PERMISSION_NAME = "permission_name";
    private static final String COL_PERMISSION_GAIN_ACCESS = "gain_access";
    private static final String COL_PERMISSION_LOST_ACCESS = "lost_access";

    //TABLE_APPS_ACTIVITY columns names
    private static final String COL_FOREGROUND_TIME_USAGE = "foreground_time_usage";
    private static final String COL_LAST_TIME_USE = "last_time_use";
    private static final String COL_UPLOADED_DATA = "uploaded_data";
    private static final String COL_DOWNLOADED_DATA = "downloaded_data";
    private static final String COL_RECORD_TIME = "record_time";
    private static final String COL_WAS_FOREGROUND = "was_foreground";
    private static final String COL_AVG_CPU_USAGE = "avg_cpu_usage";
    private static final String COL_MAX_CPU_USAGE = "max_cpu_usage";
    private static final String COL_BOOT = "boot";



    //Table GPS_LOCATION columns names
    private static final String COL_LATITUDE = "latitude";
    private static final String COL_LONGITUDE = "longitude";
    private static final String COL_ALTITUDE = "altitude";
    private static final String COL_ACCURACY = "accuracy";
    private static final String COL_GPS_ENABLED = "gps_enable";







    //Table creation SQL statement
    private static final String CREATE_TABLE_INSTALLED_APPS =
            "CREATE TABLE " + TABLE_INSTALLED_APPS + "(" +
            COL_APP_ID +" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COL_APP_PKG_NAME + " TEXT SECONDARY KEY, " +
            COL_APP_NAME + " TEXT, " +
            COL_INSTALLATION_DATE + " INTEGER, " +
            COL_UNINSTALLATION_DATE + " INTEGER, " +
            COL_IS_SYSTEM + " INTEGER" + ")";

    private static final String CREATE_TABLE_APPS_ACTIVITY =
            "CREATE TABLE " + TABLE_APPS_ACTIVITY + "("
            + COL_RECORD_ID +" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COL_APP_PKG_NAME + " TEXT, " +
            COL_RECORD_TIME + " INTEGER, " +
            COL_FOREGROUND_TIME_USAGE + " INTEGER SECONDARY KEY, " +
            COL_LAST_TIME_USE + " INTEGER," +
            COL_DOWNLOADED_DATA + " INTEGER, " +
            COL_UPLOADED_DATA + " INTEGER, " +
            COL_AVG_CPU_USAGE + " REAL, " +
            COL_MAX_CPU_USAGE + " INTEGER, " +
            COL_WAS_FOREGROUND + " INTEGER, " +
            COL_BOOT + " INTEGER"
            + ")";

    private static final String CREATE_TABLE_PERMISSIONS =
            "CREATE TABLE " + TABLE_PERMISSIONS + "(" +
            COL_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COL_APP_PKG_NAME + " TEXT SECONDARY KEY, " +
            COL_PERMISSION_NAME + " TEXT, " + COL_PERMISSION_GAIN_ACCESS +" INTEGER, " +
            COL_PERMISSION_LOST_ACCESS + " INTEGER" + ")";

    private static final String CREATE_TABLE_APPS_INTERNET_USE_LAST_TIME =
            "CREATE TABLE " + TABLE_APPS_INTERNET_USE_LAST_TIME + "("
            + COL_RECORD_ID +" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COL_APP_PKG_NAME + " TEXT, " +
            COL_RECORD_TIME + " INTEGER, " +
            COL_DOWNLOADED_DATA + " INTEGER, " +
            COL_UPLOADED_DATA + " INTEGER " +
            ")";

    private static final String CREATE_TABLE_GPS_LOCATION =
            "CREATE TABLE " + TABLE_GPS_LOCATION + "(" +
            COL_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COL_RECORD_TIME + " INTEGER, " +
            COL_GPS_ENABLED + " INTEGER, " +
            COL_LATITUDE + " REAL, " +
            COL_LONGITUDE + " REAL, " +
            COL_ALTITUDE + " REAL, " +
            COL_ACCURACY + " REAL" +
            ")";

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
        db.execSQL(CREATE_TABLE_APPS_ACTIVITY);
        db.execSQL(CREATE_TABLE_INSTALLED_APPS);
        db.execSQL(CREATE_TABLE_PERMISSIONS);
        db.execSQL(CREATE_TABLE_APPS_INTERNET_USE_LAST_TIME);
        db.execSQL(CREATE_TABLE_GPS_LOCATION);
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO
        db.execSQL("DROP TABLE " + TABLE_INSTALLED_APPS);
        db.execSQL("DROP TABLE " + TABLE_APPS_ACTIVITY);
        db.execSQL("DROP TABLE " + TABLE_PERMISSIONS);
        db.execSQL("DROP TABLE " + TABLE_APPS_INTERNET_USE_LAST_TIME);
        db.execSQL("DROP TABLE " + TABLE_GPS_LOCATION);


        db.execSQL(CREATE_TABLE_APPS_ACTIVITY);
        db.execSQL(CREATE_TABLE_INSTALLED_APPS);
        db.execSQL(CREATE_TABLE_PERMISSIONS);
        db.execSQL(CREATE_TABLE_APPS_INTERNET_USE_LAST_TIME);
        db.execSQL(CREATE_TABLE_GPS_LOCATION);

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
            LogA.i("Appspy-DB", "A new ApplicationInstallationRecord has been added");
            values.put(COL_APP_NAME, newRecord.getApplicationName());
            values.put(COL_APP_PKG_NAME, newRecord.getPackageName());
            values.put(COL_INSTALLATION_DATE, newRecord.getInstallationDate());
            values.put(COL_UNINSTALLATION_DATE, newRecord.getUninstallationDate());
            values.put(COL_IS_SYSTEM, newRecord.isSystem());

            db.insert(TABLE_INSTALLED_APPS, null, values);

        }
        //if record exists and was uninstalled (uninstallationDate > 0), update the uninstallationDate
        else if(newRecord.getUninstallationDate() == 0){
            //If it exists, as package_name is a unique identifier of an app, it means, there is already a record about it.
            // Thus we update the columns uninstallation_date, the current_permissions and the max_permissions
            LogA.i("Appspy-DB", "one applicationInstallationRecord has been updated");


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


    public List<ApplicationInstallationRecord> getAllApplicationInstallationRecords() {
        //TODO implement for real
//        SQLiteDatabase db = getReadableDatabase();
//
//        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_WAS_BACKGROUND + " = 0 ";
//
//        Cursor cursor = db.rawQuery(query, null);
//
//
//        if (cursor.moveToFirst()) {
//            do {
//                Log.d("Appspy", "IS IN DB:" + cursor.getString(cursor.getColumnIndex(COL_APP_NAME)));
//            } while (cursor.moveToNext());
//        }
        return null;
    }


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
            int appId = cursor.getInt(cursor.getColumnIndex(COL_APP_ID));
            //String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
            String appName = cursor.getString(cursor.getColumnIndex(COL_APP_NAME));
            long installationDate = cursor.getLong(cursor.getColumnIndex(COL_INSTALLATION_DATE));
            long uninstallationDate = cursor.getLong(cursor.getColumnIndex(COL_UNINSTALLATION_DATE));
            boolean appIsSystem = cursor.getInt(cursor.getColumnIndex(COL_IS_SYSTEM)) == 1;

            cursor.close();
            return new ApplicationInstallationRecord(appId, appName, packageName, installationDate, uninstallationDate,
                                                     appIsSystem);
        } else {
            //As packageName is unique, there is anyway at maximum one. The other case is
            //therefore when no record is found. In such case, we return null
            cursor.close();
            return null;
        }
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
         * if an activity occured on background. Otherwise, no record is added
         * @param newRecord new record, with down/uploaded data total for that app that day. Computation will be made autmatically
         * @return if a record was added, meaning if app was active on foreground
         */
    public void addApplicationActivityRecordIntelligent(ApplicationActivityRecord newRecord) {
        SQLiteDatabase db = this.getWritableDatabase();


        //FIRST: check if there exists a record that exits, without end time for that app
        //then verify that this record starttime is today
        //then update start time

        //of the last_use_time

        ApplicationActivityRecord lastRecord = getLastApplicationActivityRecord(newRecord.getPackageName());
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

            addLastInternetUse(newRecord.getPackageName(), newRecord.getUploadedData(), newRecord.getDownloadedData());

            //if the app was used a long time and no stat were fired for a moment, some records may say the app was in
            //background. So need to update correctly these
            long activeTime = newRecord.getForegroundTime();
            if(lastRecord !=null && wasForeground){
                activeTime = activeTime - lastRecord.getForegroundTime();
                long beginActivity = newRecord.getLastTimeUsed() - activeTime - 59000; //TODO: 60000 = interval of sampling, dynamic please
                List<ApplicationActivityRecord> records = getRecordIntImeRange(beginActivity, newRecord.getRecordTime(), newRecord.getPackageName());


                for(ApplicationActivityRecord record : records){
                    //these were on foreground. Need to update them
                    record.setWasForeground(true);
                    updateApplicationActivityRecord(record);
                    LogA.d("Appspy-DB", "one update");
                }

                LogA.d("Appspy-DB", "UPDATE foreground " + newRecord.getPackageName());

                //all these records have been active on foreground
            }

            //fix border effect when booting
            //at boot, every apps are in background if active
            //TODO verify that
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
            LogA.i("Appspy-DB", "New application activity record added for " + newRecord.getPackageName());
        }
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
     * Return the most recent record older than the one given in parameter
     * @param record the record to compare with
     * @return
     */
    public ApplicationActivityRecord getPreviousApplicationActivityRecord(ApplicationActivityRecord record){

        final String packageName = record.getPackageName();

        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_RECORD_TIME + "=" +
                       "(" +
                       "SELECT MAX(" + COL_RECORD_TIME + ") FROM " + TABLE_APPS_ACTIVITY +
                       " WHERE " + COL_APP_PKG_NAME + "=\"" + packageName + "\"" +
                       " AND " + COL_RECORD_TIME + "<" + record.getRecordTime() +
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


//    /**
//     * @param state
//     * @return
//     */
//    //TODO adapt according to state (and also system app or not system app)
//    public List<ApplicationActivityRecord> getApplicationActivityRecords(ACTIVE_STATE state, boolean includeSystem) {
////        SQLiteDatabase db = getReadableDatabase();
////
////        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_WAS_BACKGROUND + "=0";
////
////        Cursor cursor = db.rawQuery(query, null);
////
////        ArrayList<ApplicationActivityRecord> records = new ArrayList<ApplicationActivityRecord>();
////
////        if (cursor.moveToFirst()) {
////            do {
//////                int id = cursor.getInt(cursor.getColumnIndex(COL_APP_ID));
//////                String name = cursor.getString(cursor.getColumnIndex(COL_APP_NAME));
//////                String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
//////                long timestampTime = cursor.getLong(cursor.getColumnIndex(COL_TIMESTAMP));
//////                boolean wasBackground = cursor.getInt(cursor.getColumnIndex(COL_WAS_BACKGROUND)) == 1;
//////                records.add(new ApplicationActivityRecord(id,name,pkgName, timestampTime, wasBackground));
////            } while (cursor.moveToNext());
////        }
////
////        return records;
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





    public List<ApplicationActivityRecord> getRecordIntImeRange(long begining, long end, String packageName){

        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_RECORD_TIME + ">=" + begining +
                " AND " + COL_RECORD_TIME + "<=" + end + " AND " + COL_APP_PKG_NAME + "=\"" + packageName + "\"";

        Cursor result = db.rawQuery(query, null);

        ArrayList<ApplicationActivityRecord> records = new ArrayList<>();

        if(result.moveToFirst()){
            do{
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


                records.add(new ApplicationActivityRecord(recordID, packageName, recordTime, foregroundTime, lastUsed,
                                                     uploaded, downloaded, wasForeground, boot));
            } while(result.moveToNext());
        }

        result.close();
        return records;
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
    public void updatePermissionRecordsForApp(String packageName, HashMap<String, PermissionRecord> records) {
        SQLiteDatabase db = getWritableDatabase();

        //Query first to get all the permission for an app, which are currently in use (=last time used = 0)
        String query = "SELECT * FROM " + TABLE_PERMISSIONS + " WHERE " + COL_APP_PKG_NAME + "=" + "\"" +
                       packageName + "\"" + " AND " + COL_PERMISSION_LOST_ACCESS + "=0";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {

            //For all the currently used permissions in the DB for that app, we check if they are
            //in the permissions just checked. If they are, it means that they are still in used
            //thus, nothing need to be done. We remove them from the hashmap.
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

//    public enum ACTIVE_STATE {
//        ACTIVE_BACKGROUND,
//        ACTIVE_FOREGROUND,
//        ACTIVE //active not depending if the app is in background or in foreground
//    }




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
        toInsert.put(COL_GPS_ENABLED, record.getLocationType().getValue());
        db.insert(TABLE_GPS_LOCATION, null, toInsert);
        
        LogA.i("Appspy-DB", "new GPS record inserted");
        
    }


}