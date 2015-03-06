package com.epfl.appspy.com.epfl.appspy.database;

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
import java.util.HashSet;
import java.util.Hashtable;
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
    private static final int DB_VERSION = 31;
    private static final String DB_NAME = "Appspy_database";

    //Tables names
    private static final String TABLE_APPS_ACTIVITY = "Table_applications_activity";
    private static final String TABLE_INSTALLED_APPS = "Table_installed_apps";

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

    //Table creation SQL statement
    private static final String CREATE_TABLE_INSTALLED_APPS =
            "CREATE TABLE " + TABLE_INSTALLED_APPS + "(" + COL_APP_ID +
            " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + COL_APP_PKG_NAME + " TEXT SECONDARY KEY UNIQUE, " +
            COL_APP_NAME + " TEXT, " + COL_INSTALLATION_DATE + " INTEGER, " + COL_UNINSTALLATION_DATE + " INTEGER, " +
            COL_CURRENT_PERMISSIONS + " TEXT, " + COL_MAX_PERMISSIONS + " TEXT, " + COL_IS_SYSTEM + " INTEGER" + ")";

    private static final String CREATE_TABLE_APPS_ACTIVITY =
            "CREATE TABLE " + TABLE_APPS_ACTIVITY + "(" + COL_RECORD_ID +
            " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + COL_APP_PKG_NAME + " TEXT, " + COL_TIMESTAMP + " TEXT, " +
            COL_WAS_BACKGROUND + " INTEGER " + ")";


    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_APPS_ACTIVITY);
        db.execSQL(CREATE_TABLE_INSTALLED_APPS);

    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO
        Log.d("Appspy", "HEY DROP TABLE SQL");
        db.execSQL("DROP TABLE " + TABLE_INSTALLED_APPS);
        db.execSQL("DROP TABLE " + TABLE_APPS_ACTIVITY);
        db.execSQL(CREATE_TABLE_INSTALLED_APPS);
        db.execSQL(CREATE_TABLE_APPS_ACTIVITY);
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
     * @param packageName
     * @return
     */
    public boolean installationRecordExists(String packageName) {
        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT " + COL_APP_PKG_NAME + " FROM " + TABLE_INSTALLED_APPS + " WHERE " + COL_APP_PKG_NAME + " =\"" +
                packageName + "\"";

        Cursor cursor = db.rawQuery(query, null);

        return cursor.getCount() > 0;
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
            LogA.i("Appspy DB", "A new ApplicationInstallationRecord has been added");
            values.put(COL_MAX_PERMISSIONS, newRecord.getCurrentPermissions());
            values.put(COL_APP_NAME, newRecord.getApplicationName());
            values.put(COL_APP_PKG_NAME, newRecord.getPackageName());
            values.put(COL_INSTALLATION_DATE, newRecord.getInstallationDate());
            values.put(COL_UNINSTALLATION_DATE, newRecord.getUninstallationDate());
            values.put(COL_CURRENT_PERMISSIONS, newRecord.getCurrentPermissions());
            values.put(COL_IS_SYSTEM, newRecord.isSystem());

            db.insert(TABLE_INSTALLED_APPS, null, values);
        }
        else {
            //If it exists, as package_name is a unique identifier of an app, it means, there is already a record about it.
            // Thus we update the columns uninstallation_date, the current_permissions and the max_permissions
            LogA.i("Appspy DB", "one applicationInstallationRecord has been updated");


            //newRecord contains the updated values
            //oldRecord contains the one already in the DB
            ApplicationInstallationRecord oldRecord = getApplicationInstallationRecord(newRecord.getPackageName());

            //For col maxPermissions do union of currentPermission and the maximumPermission of the existing records
            HashSet<String> maxPermissions = new HashSet<>();
            List<String> currentPermissions = new PermissionsJSON(newRecord.getCurrentPermissions()).toList();
            List<String> oldMaxPermissions = new PermissionsJSON(oldRecord.getMaximumPermissions()).toList();

            maxPermissions.addAll(currentPermissions);
            maxPermissions.addAll(oldMaxPermissions);
            List<String> newMaxPermissions = new ArrayList<String>(maxPermissions);

            //update only a few columns. The other stay the same.
            values.put(COL_APP_ID, oldRecord.getAppId());
            values.put(COL_APP_NAME, oldRecord.getApplicationName());
            values.put(COL_APP_PKG_NAME, oldRecord.getPackageName());
            values.put(COL_INSTALLATION_DATE, oldRecord.getInstallationDate());
            values.put(COL_UNINSTALLATION_DATE, newRecord.getUninstallationDate()); //updated with new
            values.put(COL_IS_SYSTEM, oldRecord.isSystem());
            values.put(COL_CURRENT_PERMISSIONS, newRecord.getCurrentPermissions()); //updated with new
            values.put(COL_MAX_PERMISSIONS, new PermissionsJSON(newMaxPermissions).toString()); //updated with new

            //SQL query. Update the row for the current packageName
            String[] args = {newRecord.getPackageName()};
            db.update(TABLE_INSTALLED_APPS, values, COL_APP_PKG_NAME + " = ?", args);
        }
        db.close();
    }


    public List<ApplicationInstallationRecord> getAllApplicationInstallationRecords() {
        //TODO implement for real
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_WAS_BACKGROUND + " = 0 ";

        Cursor cursor = db.rawQuery(query, null);


        if (cursor.moveToFirst()) {
            do {
                Log.d("Appspy", "IS IN DB:" + cursor.getString(cursor.getColumnIndex(COL_APP_NAME)));
            } while (cursor.moveToNext());
        }
        return null;
    }


    /**
     * Get the ApplicationInstallationRecord about the application whose package name is provided in argument
     *
     * @param packageName the package name of the record wanted
     * @return the ApplicationInstallationRecord associated to the packageName
     */
    public ApplicationInstallationRecord getApplicationInstallationRecord(String packageName) {
        SQLiteDatabase db = getReadableDatabase();
        String query =
                "SELECT * FROM " + TABLE_INSTALLED_APPS + " WHERE " + COL_APP_PKG_NAME + "=" + "\"" + packageName +
                "\"";
        Cursor cursor = db.rawQuery(query, null);

        //Verify that the is exactly one record. As the package name is unique, there is 0 or 1 row in the cursor.
        if (cursor.moveToFirst() && cursor.getCount() == 1) {
            int appId = cursor.getInt(cursor.getColumnIndex(COL_APP_ID));
            String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
            String appName = cursor.getString(cursor.getColumnIndex(COL_APP_NAME));
            long installationDate = cursor.getLong(cursor.getColumnIndex(COL_INSTALLATION_DATE));
            long uninstallationDate = cursor.getLong(cursor.getColumnIndex(COL_UNINSTALLATION_DATE));
            boolean appIsSystem = cursor.getInt(cursor.getColumnIndex(COL_IS_SYSTEM)) == 1;
            String permissions = cursor.getString(cursor.getColumnIndex(COL_CURRENT_PERMISSIONS));
            String maxPermissions = cursor.getString(cursor.getColumnIndex(COL_MAX_PERMISSIONS));

            return new ApplicationInstallationRecord(appId, appName, packageName, installationDate, uninstallationDate,
                                                     permissions, maxPermissions, appIsSystem);
        } else {
            //As packageName is unique, there is anyway at maximum one. The other case is
            //therefore when no record is found. In such case, we return null
            return null;
        }
    }


    /**
     * Returns the id (called appID) associated with that packageName in the table of installed apps
     * @param packageName
     * @return
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


    /**
     * @param record
     */
    public void addApplicationActivityRecord(ApplicationActivityRecord record) {
        LogA.i("Appspy DB", "A new AppActiveTimestamp record has been added");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //id will be created, none exist for the new record
        values.put(COL_APP_PKG_NAME, record.getPackageName());
        values.put(COL_TIMESTAMP, record.getUseTime());
        values.put(COL_WAS_BACKGROUND, record.isBackground());

        db.insert(TABLE_APPS_ACTIVITY, null, values);
        db.close();
    }


    /**
     * @param state
     * @return
     */
    //TODO adapt according to state (and also system app or not system app)
    public List<ApplicationActivityRecord> getApplicationActivityRecords(ACTIVE_STATE state, boolean includeSystem) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_WAS_BACKGROUND + "=0";

        Cursor cursor = db.rawQuery(query, null);

        ArrayList<ApplicationActivityRecord> records = new ArrayList<ApplicationActivityRecord>();

        if (cursor.moveToFirst()) {
            do {
//                int id = cursor.getInt(cursor.getColumnIndex(COL_APP_ID));
//                String name = cursor.getString(cursor.getColumnIndex(COL_APP_NAME));
//                String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
//                long timestampTime = cursor.getLong(cursor.getColumnIndex(COL_TIMESTAMP));
//                boolean wasBackground = cursor.getInt(cursor.getColumnIndex(COL_WAS_BACKGROUND)) == 1;
//                records.add(new ApplicationActivityRecord(id,name,pkgName, timestampTime, wasBackground));
            } while (cursor.moveToNext());
        }

        return records;
    }

    //returns the list of timestamps for that app


    public enum ACTIVE_STATE {
        ACTIVE_BACKGROUND,
        ACTIVE_FOREGROUND,
        ACTIVE //active not depending if the app is in background or in foreground
    }


}
