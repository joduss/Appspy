package com.epfl.appspy.database;

import com.epfl.appspy.LocationType;

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
    private LocationType locationType;


    public GPSRecord(long id, long recordTime, LocationType locationType, double longitude, double latitude, double altitude,
                     float accuracy) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.recordTime = recordTime;
        this.accuracy = accuracy;
        this.id = id;
        this.locationType = locationType;
        this.altitude = altitude;

        LocationType t = locationType;

    }

    public GPSRecord(long recordTime, LocationType locationType, double longitude, double latitude,
                     double altitude, float accuracy) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.recordTime = recordTime;
        this.accuracy = accuracy;
        this.locationType = locationType;
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


    public LocationType getLocationType() {
        return locationType;
    }


    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }
}
