package com.epfl.appspy.com.epfl.appspy.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage the database of the app
 *
 * There is 1 table:
 *         - TABLE_APP_ACTIVE_TIMESTAMPS, stores timestamps, where the app was active (background, or foreground)
 *
* Created by Jonathan Duss on 22.02.15.
*/
public class Database extends SQLiteOpenHelper {

    public enum ACTIVE_STATE {
        ACTIVE_BACKGROUND,
        ACTIVE_FOREGROUND,
        ACTIVE //active not dependinf if the app is in background or in foreground
    }


    private static final int DB_VERSION = 2;

    //Database name
    private static final String DB_NAME = "DATABASE";

    //Tables names
    private static final String TABLE_APP_ACTIVE_TIMESTAMPS = "TABLE_APP_ACTIVE_TIMESTAMPS";
    //...

    //TABLE_APP_ACTIVE_TIMESTAMPS columns names
    private static final String COL_KEY_ID = "key_id";
    private static final String COL_APP_NAME = "app_name";
    private static final String COL_APP_PKG_NAME = "package_name";
    private static final String COL_TIMESTAMP = "use_time";
    private static final String COL_WAS_BACKGROUND = "was_background";

    private static final String CREATE_TABLE_APP_ACTIVE_TIMESTAMPS = "CREATE TABLE " + TABLE_APP_ACTIVE_TIMESTAMPS
            + "(" + COL_KEY_ID + " INTEGER PRIMARY KEY, " + COL_APP_NAME + " TEXT, " + COL_APP_PKG_NAME + " TEXT, "
            + COL_TIMESTAMP + " INTEGER, " + COL_WAS_BACKGROUND + " INTEGER)";


    public Database(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_APP_ACTIVE_TIMESTAMPS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //TODO
        db.execSQL("DROP TABLE " + TABLE_APP_ACTIVE_TIMESTAMPS);
        db.execSQL(CREATE_TABLE_APP_ACTIVE_TIMESTAMPS);
    }

    public void addApplicationActiveTimestamp(ApplicationUseRecord record){
        Log.i("Appspy DB", "new AppActiveTimestamp record added in Table " + TABLE_APP_ACTIVE_TIMESTAMPS);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //id will be created, none exist for the new record
        values.put(COL_APP_NAME, record.getApplicationName());
        values.put(COL_APP_PKG_NAME, record.getPackageName());
        values.put(COL_TIMESTAMP, record.getUseTime());
        values.put(COL_WAS_BACKGROUND, record.isBackground());

        db.insert(TABLE_APP_ACTIVE_TIMESTAMPS, null, values);
        db.close();
    }

    //returns the list of timestamps for that app
    public List<ApplicationUseRecord> getApplicationActiveTimestamp(ACTIVE_STATE state){
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APP_ACTIVE_TIMESTAMPS + " WHERE " + COL_WAS_BACKGROUND + " = 0 ";

        Cursor cursor = db.rawQuery(query, null);

        ArrayList<ApplicationUseRecord> records = new ArrayList<ApplicationUseRecord>();

        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndex(COL_KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(COL_APP_NAME));
                String pkgName = cursor.getString(cursor.getColumnIndex(COL_APP_PKG_NAME));
                long timestampTime = cursor.getLong(cursor.getColumnIndex(COL_TIMESTAMP));
                boolean wasBackground = cursor.getInt(cursor.getColumnIndex(COL_WAS_BACKGROUND)) == 1;
                records.add(new ApplicationUseRecord(id,name,pkgName, timestampTime, wasBackground));
            } while(cursor.moveToNext());
        }

        return records;
    }



}
