//package com.epfl.appspy.com.epfl.appspy.database;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Manage the database of the app
// *
// * There is 1 table:
// *         - TABLE_APPS_ACTIVITY, stores timestamps, where the app was active (background, or foreground)
// *
//* Created by Jonathan Duss on 22.02.15.
//*/
//public class ApplicationActivityRecordsDatabase extends SQLiteOpenHelper {
//
//    public enum ACTIVE_STATE {
//        ACTIVE_BACKGROUND,
//        ACTIVE_FOREGROUND,
//        ACTIVE //active not depending if the app is in background or in foreground
//    }
//
//    //Database version
//    private static final int DB_VERSION = 1;
//
//    private static final String DB_NAME = "Appspy database";
//
//    //Tables names
//    private static final String TABLE_APPS_ACTIVITY = "Table applications activity";
//    private static final String TABLE_INSTALLED_APPS = "Table installed apps";
//    //...
//
//    //TABLE_APPS_ACTIVITY columns names
//    private static final String COL_APP_ID = "app_id";
//    private static final String COL_RECORD_ID = "record_id";
//    private static final String COL_APP_NAME = "app_name";
//    private static final String COL_APP_PKG_NAME = "package_name";
//    private static final String COL_TIMESTAMP = "use_time";
//    private static final String COL_WAS_BACKGROUND = "was_background";
//    private static final String COL_INSTALLATION_DATE = "installation date";
//    private static final String COL_UNINSTALLATION_DATE = "uninstallation date";
//    private static final String COL_CURRENT_PERMISSIONS = "current permissions";
//    private static final String COL_MAX_PERMISSIONS = "maximum permissions";
//
//    private static final String CREATE_TABLE_APPS_ACTIVITY = "CREATE TABLE" + TABLE_APPS_ACTIVITY + "(" + COL_RECORD_ID
//            + " INTEGER PRIMARY_KEY, " + COL_TIMESTAMP + " TEXT" + ")";
//
//    private static final String CREATE_TABLE_INSTALLED_APPS = "CREATE TABLE " + TABLE_INSTALLED_APPS
//            + "(" + COL_APP_ID + " INTEGER PRIMARY KEY, " + COL_APP_PKG_NAME + " TEXT, " + COL_APP_NAME + " TEXT, "
//            + COL_INSTALLATION_DATE + " INTEGER, " + COL_UNINSTALLATION_DATE + " INTEGER, " + COL_CURRENT_PERMISSIONS + " TEXT, " + COL_MAX_PERMISSIONS + " TEXT" + ")";
//
//
//    public ApplicationActivityRecordsDatabase(Context context){
//        super(context, DB_NAME, null, DB_VERSION);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(CREATE_TABLE_APPS_ACTIVITY);
//        db.execSQL(CREATE_TABLE_INSTALLED_APPS);
//
//    }
//
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
//        //TODO
//    }
//
//    public void addApplicationActiveTimestamp(ApplicationActivityRecord record){
//        Log.i("Appspy DB", "new AppActiveTimestamp record added in Table " + TABLE_APPS_ACTIVITY);
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        //id will be created, none exist for the new record
//        values.put(COL_APP_NAME, record.getApplicationName());
//        values.put(COL_APP_PKG_NAME, record.getPackageName());
//        values.put(COL_TIMESTAMP, record.getUseTime());
//        values.put(COL_WAS_BACKGROUND, record.isBackground());
//
//        db.insert(TABLE_APPS_ACTIVITY, null, values);
//        db.close();
//    }
//
//    //returns the list of timestamps for that app
//    public List<ApplicationActivityRecord> getApplicationActiveTimestamp(ACTIVE_STATE state){
//        SQLiteDatabase db = getReadableDatabase();
//
//        String query = "SELECT * FROM " + TABLE_APPS_ACTIVITY + " WHERE " + COL_WAS_BACKGROUND + " = 0 ";
//
//        Cursor cursor = db.rawQuery(query, null);
//
//        ArrayList<ApplicationActivityRecord> records = new ArrayList<ApplicationActivityRecord>();
//
//        if(cursor.moveToFirst()){
//            do{
//                int id = cursor.getInt(cursor.getColumnIndex(COL_APP_ID));
//                String name = cursor.getString(cursor.getColumnIndex(COL_APP_NAME));
//                String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
//                long timestampTime = cursor.getLong(cursor.getColumnIndex(COL_TIMESTAMP));
//                boolean wasBackground = cursor.getInt(cursor.getColumnIndex(COL_WAS_BACKGROUND)) == 1;
//                records.add(new ApplicationActivityRecord(id,name,pkgName, timestampTime, wasBackground));
//            } while(cursor.moveToNext());
//        }
//
//        return records;
//    }
//
//
//
//}
