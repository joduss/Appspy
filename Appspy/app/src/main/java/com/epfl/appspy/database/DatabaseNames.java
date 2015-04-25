package com.epfl.appspy.database;

/**
 * Created by Jonathan Duss on 25.04.15.
 */
public class DatabaseNames {


    //Database version
    protected static final int DB_VERSION = 155;
    protected static final String DB_NAME = "Appspy_database";

    //Tables names
    protected static final String TABLE_APPS_ACTIVITY = "Table_applications_activity";
    protected static final String TABLE_INSTALLED_APPS = "Table_installed_apps";
    protected static final String TABLE_PERMISSIONS = "Table_permissions";
    protected static final String TABLE_APPS_INTERNET_USE_LAST_TIME = "Table_internet_use_last_time";
    protected static final String TABLE_GPS_LOCATION = "Table_GPS_location";

    //SHARED columns names
    protected static final String COL_RECORD_ID = "record_id"; //id in any table, except in installed apps
    protected static final String COL_APP_NAME = "app_name";
    protected static final String COL_APP_PKG_NAME = "package_name";


    //INSTALLED APP TABLE columns names
    protected static final String COL_APP_ID = "app_id"; //id in installed app
    protected static final String COL_IS_SYSTEM = "is_system";
    protected static final String COL_INSTALLATION_DATE = "installation_date";
    protected static final String COL_UNINSTALLATION_DATE = "uninstallation_date";

    //PERMISSIONS TABLE columns names
    protected static final String COL_PERMISSION_NAME = "permission_name";
    protected static final String COL_PERMISSION_GAIN_ACCESS = "gain_access";
    protected static final String COL_PERMISSION_LOST_ACCESS = "lost_access";

    //TABLE_APPS_ACTIVITY columns names
    protected static final String COL_FOREGROUND_TIME_USAGE = "foreground_time_usage";
    protected static final String COL_LAST_TIME_USE = "last_time_use";
    protected static final String COL_UPLOADED_DATA = "uploaded_data";
    protected static final String COL_DOWNLOADED_DATA = "downloaded_data";
    protected static final String COL_RECORD_TIME = "record_time";
    protected static final String COL_WAS_FOREGROUND = "was_foreground";
    protected static final String COL_AVG_CPU_USAGE = "avg_cpu_usage";
    protected static final String COL_MAX_CPU_USAGE = "max_cpu_usage";
    protected static final String COL_BOOT = "boot";


    //Table GPS_LOCATION columns names
    protected static final String COL_LATITUDE = "latitude";
    protected static final String COL_LONGITUDE = "longitude";
    protected static final String COL_ALTITUDE = "altitude";
    protected static final String COL_ACCURACY = "accuracy";
    protected static final String COL_GPS_ENABLED = "location_provider";


    //Table creation SQL statement
    protected static final String CREATE_TABLE_INSTALLED_APPS = "CREATE TABLE " + TABLE_INSTALLED_APPS + "(" +
                                                                COL_APP_ID +
                                                                " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                                                COL_APP_PKG_NAME + " TEXT SECONDARY KEY, " +
                                                                COL_APP_NAME + " TEXT, " +
                                                                COL_INSTALLATION_DATE + " INTEGER, " +
                                                                COL_UNINSTALLATION_DATE + " INTEGER, " +
                                                                COL_IS_SYSTEM + " INTEGER" + ")";

    protected static final String CREATE_TABLE_APPS_ACTIVITY =
            "CREATE TABLE " + TABLE_APPS_ACTIVITY + "(" + COL_RECORD_ID +
            " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COL_APP_PKG_NAME + " TEXT, " +
            COL_RECORD_TIME + " INTEGER, " +
            COL_FOREGROUND_TIME_USAGE + " INTEGER SECONDARY KEY, " +
            COL_LAST_TIME_USE + " INTEGER," +
            COL_DOWNLOADED_DATA + " INTEGER, " +
            COL_UPLOADED_DATA + " INTEGER, " +
            COL_AVG_CPU_USAGE + " REAL, " +
            COL_MAX_CPU_USAGE + " INTEGER, " +
            COL_WAS_FOREGROUND + " INTEGER, " +
            COL_BOOT + " INTEGER" + ")";

    protected static final String CREATE_TABLE_PERMISSIONS = "CREATE TABLE " + TABLE_PERMISSIONS + "(" +
                                                             COL_RECORD_ID +
                                                             " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                                             COL_APP_PKG_NAME + " TEXT SECONDARY KEY, " +
                                                             COL_PERMISSION_NAME + " TEXT, " +
                                                             COL_PERMISSION_GAIN_ACCESS + " INTEGER, " +
                                                             COL_PERMISSION_LOST_ACCESS + " INTEGER" + ")";

    protected static final String CREATE_TABLE_APPS_INTERNET_USE_LAST_TIME =
            "CREATE TABLE " + TABLE_APPS_INTERNET_USE_LAST_TIME + "(" + COL_RECORD_ID +
            " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COL_APP_PKG_NAME + " TEXT, " +
            COL_RECORD_TIME + " INTEGER, " +
            COL_DOWNLOADED_DATA + " INTEGER, " +
            COL_UPLOADED_DATA + " INTEGER " +
            ")";

    protected static final String CREATE_TABLE_GPS_LOCATION = "CREATE TABLE " + TABLE_GPS_LOCATION + "(" +
                                                              COL_RECORD_ID +
                                                              " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                                              COL_RECORD_TIME + " INTEGER, " +
                                                              COL_GPS_ENABLED + " INTEGER, " +
                                                              COL_LATITUDE + " REAL, " +
                                                              COL_LONGITUDE + " REAL, " +
                                                              COL_ALTITUDE + " REAL, " +
                                                              COL_ACCURACY + " REAL" +
                                                              ")";


    protected static final String VIEW_APP_ACTIVITY = "view_app_activity";
    protected static final String CREATE_VIEW_APP_ACTIVITY = "CREATE VIEW " + VIEW_APP_ACTIVITY + " AS SELECT "+ COL_RECORD_ID + "," + COL_APP_PKG_NAME + "," +
                                                      "datetime(" + COL_RECORD_TIME +
                                                      " / 1000, 'unixepoch', 'localtime') AS " + COL_RECORD_TIME +  "," +
                                                      "time(" + COL_FOREGROUND_TIME_USAGE +
                                                      " / 1000, 'unixepoch', 'localtime') AS " + COL_FOREGROUND_TIME_USAGE +  "," +
                                                      "datetime(" + COL_LAST_TIME_USE +
                                                      "/1000, 'unixepoch', 'localtime') AS " + COL_LAST_TIME_USE +  "," +
                                                      COL_DOWNLOADED_DATA + "*1.0/1024" + " as "+ COL_DOWNLOADED_DATA + "_kb," +
                                                      COL_UPLOADED_DATA + "*1.0/1024" + " as "+ COL_UPLOADED_DATA + "_kb," +
                                                      COL_WAS_FOREGROUND +
                                                      "  FROM " + TABLE_APPS_ACTIVITY;

    protected static final String VIEW_GPS = "view_gps";
    protected static final String CREATE_VIEW_GPS = "CREATE VIEW " + VIEW_GPS + " AS SELECT " + COL_RECORD_ID + ", " + "datetime(" + COL_RECORD_TIME + ", 'unixepoch', 'localtime') as " + COL_RECORD_TIME
            +"," + COL_GPS_ENABLED +"," +  COL_LATITUDE +"," + COL_LONGITUDE +"," + COL_ALTITUDE +"," + COL_ACCURACY + " FROM " + TABLE_GPS_LOCATION;

    protected static final String VIEW_INSTALLED_APPS = "view_installed_apps";
    protected static final String CREATE_VIEW_INSTALLED_APPS =
            "CREATE VIEW " + VIEW_INSTALLED_APPS + " AS  SELECT " + COL_APP_ID + "," + COL_APP_PKG_NAME + "," +
            COL_APP_NAME + "," + "datetime(" + COL_INSTALLATION_DATE + "/1000, 'unixepoch', 'localtime') as " +
            COL_INSTALLATION_DATE + ", datetime(" + COL_UNINSTALLATION_DATE + "/1000, 'unixepoch', 'localtime') as " +
            COL_UNINSTALLATION_DATE + ", " + COL_IS_SYSTEM + " FROM " + TABLE_INSTALLED_APPS;

    protected static final String VIEW_PERMISSIONS = "view_permissions";
    protected static final String CREATE_VIEW_PERMISSIONS =
            "CREATE VIEW " + VIEW_PERMISSIONS + " AS SELECT " + COL_RECORD_ID + "," + COL_APP_PKG_NAME + "," +
            COL_PERMISSION_NAME + ", datetime(" + COL_PERMISSION_GAIN_ACCESS + "/1000, 'unixepoch', 'localtime') as " +
            COL_PERMISSION_GAIN_ACCESS + ", datetime(" + COL_PERMISSION_LOST_ACCESS + "/1000, 'unixepoch', 'localtime') as" +
            COL_PERMISSION_LOST_ACCESS + " from " + TABLE_PERMISSIONS;



}
