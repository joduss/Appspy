package com.epfl.appspy.com.epfl.appspy.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jo on 22.02.15.
 */
public class Database extends SQLiteOpenHelper {


    private static final int DB_VERSION = 1;

    //Database name
    private static final String DB_NAME = "DATABASE";

    //Tables names
    private static final String APP_USE_TIME_TABLE = "app_use_time_table";
    //...

    //APP_USE_TIME_TABLE columns names
    private static final String COL_KEY_ID = "key_id";
    private static final String COL_APP_NAME = "app_name";
    private static final String COL_APP_PKG_NAME = "package_name";
    private static final String COL_USE_TIME = "use_time";
    private static final String COL_WAS_BACKGROUND = "was_background";

    private static final String CREATE_APP_USE_TIME_TABLE = "CREATE TABLE " + APP_USE_TIME_TABLE
            + "(" + COL_KEY_ID + "INTEGER PRIMARY KEY" + COL_APP_NAME + "TEXT" + COL_APP_PKG_NAME + "TEXT"
            + COL_USE_TIME + "DATETIME" + COL_WAS_BACKGROUND + "INTEGER";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_APP_USE_TIME_TABLE);
    }

    public void addApplicationUseTimeRecord(ApplicationUseRecord record){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_KEY_ID, record.getId());
        values.put(COL_APP_NAME, record.getApplicationName());
        values.put(COL_APP_PKG_NAME, record.getPackageName());
        values.put(COL_USE_TIME, record.getUseTime());
        values.put(COL_WAS_BACKGROUND, record.isBackground());
    }



}
