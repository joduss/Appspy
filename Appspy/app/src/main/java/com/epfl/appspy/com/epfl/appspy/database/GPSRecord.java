package com.epfl.appspy.com.epfl.appspy.database;

/**
 * Created by Jonathan Duss on 30.03.15.
 */
public class GPSRecord {

    private double longitude;
    private double latitude;
    private long recordTime;
    private String packageName;
    private double altitude;
    private float accuracy;
    private long id;
    private boolean gpsActivated;


    public GPSRecord(long id, String packageName, long recordTime, boolean gpsActivated, double longitude, double latitude, double altitude,
                     float accuracy) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.recordTime = recordTime;
        this.packageName = packageName;
        this.accuracy = accuracy;
        this.id = id;
        this.gpsActivated = gpsActivated;
        this.altitude = altitude;
    }

    public GPSRecord(String packageName, long recordTime, boolean gpsActivated, double longitude, double latitude,
                     double altitude, float accuracy) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.recordTime = recordTime;
        this.packageName = packageName;
        this.accuracy = accuracy;
        this.gpsActivated = gpsActivated;
        this.altitude = altitude;
    }


    public double getLongitude() {
        return longitude;
    }


    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public double getLatitude() {
        return latitude;
    }


    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public long getRecordTime() {
        return recordTime;
    }


    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }


    public String getPackageName() {
        return packageName;
    }


    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public double getAltitude() {
        return altitude;
    }


    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }


    public float getAccuracy() {
        return accuracy;
    }


    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public boolean isGpsActivated() {
        return gpsActivated;
    }


    public void setGpsActivated(boolean gpsActivated) {
        this.gpsActivated = gpsActivated;
    }
}
