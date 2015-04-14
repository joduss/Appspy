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
    private long uploadedData;
    private long downloadedData;
    private long recordTime;
    private double avgCpuUsage;
    private int maxCpuUsage;
    private boolean wasForeground;
    private boolean boot;


    /**
     *  @param recordId
     * @param packageName
     * @param foregroundTime
     * @param lastTimeUsed
     * @param uploadedData
     * @param downloadedData
     */
    protected ApplicationActivityRecord(long recordId, String packageName, long recordTime, long foregroundTime, long lastTimeUsed, long uploadedData, long downloadedData,
                                        boolean wasForeground, boolean boot) {
        this.recordId = recordId;
        this.packageName = packageName;
        this.foregroundTime = foregroundTime;
        this.lastTimeUsed = lastTimeUsed;
        this.uploadedData = uploadedData;
        this.downloadedData = downloadedData;
        this.recordTime = recordTime;

        this.wasForeground = wasForeground;
        this.boot = boot;
        avgCpuUsage = -1;
        maxCpuUsage = -1;
    }


    /**
     * @param packageName
     * @param recordTime
     * @param foregroundTime
     * @param lastTimeUsed
     * @param uploadedData
     * @param downloadedData
     * @param wasForeground
     */
    public ApplicationActivityRecord(String packageName, long recordTime, long foregroundTime, long lastTimeUsed, long uploadedData, long downloadedData,
                                     boolean wasForeground, boolean boot) {
        this.packageName = packageName;
        this.foregroundTime = foregroundTime;
        this.lastTimeUsed = lastTimeUsed;
        this.uploadedData = uploadedData;
        this.downloadedData = downloadedData;
        this.recordTime = recordTime;

        this.wasForeground = wasForeground;
        this.boot = boot;
        avgCpuUsage = -1;
        maxCpuUsage = -1;
    }


    public ApplicationActivityRecord(String packageName, long recordTime, long foregroundTime, long lastTimeUsed, long uploadedData, long downloadedData,
                                     boolean boot) {
        this.packageName = packageName;
        this.foregroundTime = foregroundTime;
        this.lastTimeUsed = lastTimeUsed;
        this.uploadedData = uploadedData;
        this.downloadedData = downloadedData;
        this.recordTime = recordTime;

        this.boot = boot;
        avgCpuUsage = -1;
        maxCpuUsage = -1;
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


    public boolean isWasForeground() {
        return wasForeground;
    }


    public void setWasForeground(boolean wasForeground) {
        this.wasForeground = wasForeground;
    }


    public double getAvgCpuUsage() {
        return avgCpuUsage;
    }


    public void setAvgCpuUsage(double avgCpuUsage) {
        this.avgCpuUsage = avgCpuUsage;
    }


    public int getMaxCpuUsage() {
        return maxCpuUsage;
    }


    public void setMaxCpuUsage(int maxCpuUsage) {
        this.maxCpuUsage = maxCpuUsage;
    }


    public boolean isBoot() {
        return boot;
    }


    public void setBoot(boolean boot) {
        this.boot = boot;
    }
}
