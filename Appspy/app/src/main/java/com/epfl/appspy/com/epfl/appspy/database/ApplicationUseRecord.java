package com.epfl.appspy.com.epfl.appspy.database;

/**
 * Created by Jo on 22.02.15.
 */
public class ApplicationUseRecord {

    private int id;
    private String applicationName;
    private String packageName;
    private long useTime;
    private boolean background;

    public ApplicationUseRecord(int id, String applicationName, String packageName, long useTime, boolean background){
        this.id = id;
        this.applicationName = applicationName;
        this.packageName = packageName;
        this.useTime = useTime;
        this.background = background;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getApplicationName() {
        return applicationName;
    }


    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }


    public String getPackageName() {
        return packageName;
    }


    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public long getUseTime() {
        return useTime;
    }


    public void setUseTime(long useTime) {
        this.useTime = useTime;
    }


    public boolean isBackground() {
        return background;
    }


    public void setBackground(boolean background) {
        this.background = background;
    }
}
