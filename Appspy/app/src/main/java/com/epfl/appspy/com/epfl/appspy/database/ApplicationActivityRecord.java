package com.epfl.appspy.com.epfl.appspy.database;

/**
 * Created by Jo on 22.02.15.
 */

/**
 * This class is a record of the activity of an app at a given time. Ex: MeteoSwiss, was active in background, 20.1.2015
 * at 22:30 Facebook, was active in foreground, 20.1.2015 at 22:30
 */
public class ApplicationActivityRecord {


    private long recordId;
    private String packageName;
    private long foregroundTime;
    private long lastTimeUsed;
    long uploadedData;
    long downloadedData;
    long recordTime;


    /**
     *
     * @param recordId
     * @param packageName
     * @param foregroundTime
     * @param lastTimeUsed
     * @param uploadedData
     * @param downloadedData
     */
    protected ApplicationActivityRecord(long recordId, String packageName, long recordTime, long foregroundTime, long lastTimeUsed, long uploadedData, long downloadedData) {
        this.recordId = recordId;
        this.packageName = packageName;
        this.foregroundTime = foregroundTime;
        this.lastTimeUsed = lastTimeUsed;
        this.uploadedData = uploadedData;
        this.downloadedData = downloadedData;
        this.recordTime = recordTime;
    }


    /**
     *
     * @param packageName
     * @param foregroundTime
     * @param lastTimeUsed
     * @param uploadedData
     * @param downloadedData
     */
    public ApplicationActivityRecord(String packageName, long recordTime, long foregroundTime, long lastTimeUsed, long uploadedData, long downloadedData) {
        this.packageName = packageName;
        this.foregroundTime = foregroundTime;
        this.lastTimeUsed = lastTimeUsed;
        this.uploadedData = uploadedData;
        this.downloadedData = downloadedData;
        this.recordTime = recordTime;
    }


    public long getRecordId() {
        return recordId;
    }


    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }


    public String getPackageName() {
        return packageName;
    }


    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public long getForegroundTime() {
        return foregroundTime;
    }


    public void setForegroundTime(long foregroundTime) {
        this.foregroundTime = foregroundTime;
    }

    public long getLastTimeUsed() {
        return lastTimeUsed;
    }


    public void setLastTimeUsed(long lastTimeUsed) {
        this.lastTimeUsed = lastTimeUsed;
    }


    public long getUploadedData() {
        return uploadedData;
    }


    public void setUploadedData(long uploadedData) {
        this.uploadedData = uploadedData;
    }


    public long getDownloadedData() {
        return downloadedData;
    }


    public void setDownloadedData(long downloadedData) {
        this.downloadedData = downloadedData;
    }


    public long getRecordTime() {
        return recordTime;
    }


    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }
}
