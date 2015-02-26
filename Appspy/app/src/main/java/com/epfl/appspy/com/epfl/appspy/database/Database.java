package com.epfl.appspy.com.epfl.appspy.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Manage the database of the app
 *
 * There is 1 table:
 *         - TABLE_APPS_ACTIVITY, stores timestamps, where the app was active (background, or foreground)
 *
 * Created by Jonathan Duss on 22.02.15.
 */
public class Database extends SQLiteOpenHelper {

    public enum ACTIVE_STATE {
        ACTIVE_BACKGROUND,
        ACTIVE_FOREGROUND,
        ACTIVE //active not depending if the app is in background or in foreground
    }

    //Database version
    private static final int DB_VERSION = 20;

    private static final String DB_NAME = "Appspy_database";

    //Tables names
    private static final String TABLE_APPS_ACTIVITY = "Table_applications_activity";
    private static final String TABLE_INSTALLED_APPS = "Table_installed_apps";
    //...

    //TABLE_APPS_ACTIVITY columns names
    private static final String COL_APP_ID = "app_id";
    private static final String COL_RECORD_ID = "record_id";
    private static final String COL_APP_NAME = "app_name";
    private static final String COL_APP_PKG_NAME = "package_name";
    private static final String COL_TIMESTAMP = "use_time";
    private static final String COL_WAS_BACKGROUND = "was_background";
    private static final String COL_INSTALLATION_DATE = "installation_date";
    private static final String COL_UNINSTALLATION_DATE = "uninstallation_date";
    private static final String COL_CURRENT_PERMISSIONS = "current_permissions";
    private static final String COL_MAX_PERMISSIONS = "maximum_permissions";
    private static final String COL_IS_SYSTEM = "is_system";

    private static final String CREATE_TABLE_APPS_ACTIVITY = "CREATE TABLE " + TABLE_APPS_ACTIVITY + "(" + COL_RECORD_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + COL_TIMESTAMP + " TEXT" + ")";

    private static final String CREATE_TABLE_INSTALLED_APPS = "CREATE TABLE " + TABLE_INSTALLED_APPS
            + "(" + COL_APP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + COL_APP_PKG_NAME + " TEXT SECONDARY KEY UNIQUE, " + COL_APP_NAME + " TEXT, "
            + COL_INSTALLATION_DATE + " INTEGER, " + COL_UNINSTALLATION_DATE + " INTEGER, " + COL_CURRENT_PERMISSIONS + " TEXT, " + COL_MAX_PERMISSIONS + " TEXT, " + COL_IS_SYSTEM + " INTEGER" + ")";


    public Database(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_APPS_ACTIVITY);
        db.execSQL(CREATE_TABLE_INSTALLED_APPS);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //TODO
        Log.d("Appspy","HEY DROP TABLE SQL");
        db.execSQL("DROP TABLE " + TABLE_INSTALLED_APPS);
        db.execSQL("DROP TABLE " + TABLE_APPS_ACTIVITY);
        db.execSQL(CREATE_TABLE_APPS_ACTIVITY);
        db.execSQL(CREATE_TABLE_INSTALLED_APPS);
    }


    /**
     *
     * @param packageInfo
     * @return
     * @throws Resources.NotFoundException the database of installed apps contains no information about this package
     */
    public int getAppIdForPackage(PackageInfo packageInfo) throws Resources.NotFoundException{
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + COL_APP_ID + "," + COL_APP_PKG_NAME + " FROM " + TABLE_INSTALLED_APPS;

        Cursor cursor = db.rawQuery(query,null);

        if(cursor.moveToFirst()){
            do{
                int appId = cursor.getInt(cursor.getColumnIndex(COL_APP_ID));
                String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));

                if(pkgName.equals(packageInfo.packageName)){
                    return appId;
                }
            } while(cursor.moveToNext());
        }

        //If nothing found, that means there is an error
        throw new Resources.NotFoundException();
    }


    /**
     *
     * @param packageName
     * @return
     */
    public boolean installationRecordExists(String packageName) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + COL_APP_PKG_NAME + " FROM " + TABLE_INSTALLED_APPS + " WHERE " + COL_APP_PKG_NAME + " =\"" + packageName + "\"";

        Cursor cursor = db.rawQuery(query,null);

//        if(cursor.moveToFirst()){
//            do{
//                int appId = cursor.getInt(cursor.getColumnIndex(COL_APP_ID));
//                String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
//
//                if(pkgName.equals(packageName)){
//                    return true;
//                }
//            } while(cursor.moveToNext());
//        }

        return cursor.getCount() > 0;
    }

    //TODO: also update instead create a new
    public void addOrUpdateApplicationInstallationRecord(ApplicationInstallationRecord record){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //id will be created, none exist for the new record
        values.put(COL_APP_NAME, record.getApplicationName());
        values.put(COL_APP_PKG_NAME, record.getPackageName());
        values.put(COL_INSTALLATION_DATE, record.getInstallationDate());
        values.put(COL_UNINSTALLATION_DATE, record.getUninstallationDate());
        values.put(COL_CURRENT_PERMISSIONS, record.getCurrentPermissions());
        values.put(COL_MAX_PERMISSIONS, record.getMaximumPermissions());
        values.put(COL_IS_SYSTEM, record.isSystem());

        if(installationRecordExists(record.getPackageName()) == false) {
            Log.i("Appspy DB", "A new ApplicationInstallationRecord has been added");
            db.insert(TABLE_INSTALLED_APPS, null, values);
        }
        else {
            String[] args = {record.getPackageName()};
            db.update(TABLE_INSTALLED_APPS, values, COL_APP_PKG_NAME + " = ?", args);
            Log.i("Appspy DB", "one applicationInstallationRecord has been updated");
        }
        db.close();
    }

    public List<ApplicationInstallationRecord> getAllApplicationInstallationRecords(){
        //TODO implement for real
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_WAS_BACKGROUND + " = 0 ";

        Cursor cursor = db.rawQuery(query, null);


        if(cursor.moveToFirst()) {
            do {
                Log.d("Appspy", "IS IN DB:" + cursor.getString(cursor.getColumnIndex(COL_APP_NAME)));

            } while(cursor.moveToNext());
        }

        return null;
    }


    /**
     *
     * @param record
     */
    public void addApplicationActivityRecord(ApplicationActivityRecord record){
        Log.i("Appspy DB", "A new AppActiveTimestamp record has been added");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //id will be created, none exist for the new record
        values.put(COL_APP_ID, record.getAppId());
        values.put(COL_TIMESTAMP, record.getUseTime());
        values.put(COL_WAS_BACKGROUND, record.isBackground());
        db.insert(TABLE_APPS_ACTIVITY, null, values);
        db.close();
    }

    //returns the list of timestamps for that app


    /**
     *
     * @param state
     * @return
     */
    //TODO adapt according to state (and also system app or not system app)
    public List<ApplicationActivityRecord> getApplicationActivityRecords(ACTIVE_STATE state, boolean includeSystem){
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_WAS_BACKGROUND + "=0";

        Cursor cursor = db.rawQuery(query, null);

        ArrayList<ApplicationActivityRecord> records = new ArrayList<ApplicationActivityRecord>();

        if(cursor.moveToFirst()){
            do{
//                int id = cursor.getInt(cursor.getColumnIndex(COL_APP_ID));
//                String name = cursor.getString(cursor.getColumnIndex(COL_APP_NAME));
//                String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
//                long timestampTime = cursor.getLong(cursor.getColumnIndex(COL_TIMESTAMP));
//                boolean wasBackground = cursor.getInt(cursor.getColumnIndex(COL_WAS_BACKGROUND)) == 1;
//                records.add(new ApplicationActivityRecord(id,name,pkgName, timestampTime, wasBackground));
            } while(cursor.moveToNext());
        }

        return records;
    }



}
