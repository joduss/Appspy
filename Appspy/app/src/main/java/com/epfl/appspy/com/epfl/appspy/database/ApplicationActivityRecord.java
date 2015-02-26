package com.epfl.appspy.com.epfl.appspy.database;

/**
 * Created by Jo on 22.02.15.
 */

/**
 * This class is a record of the activity of an app at a given time.
 * Ex: MeteoSwiss, was active in background, 20.1.2015 at 22:30
 *     Facebook, was active in foreground, 20.1.2015 at 22:30
*
 */
public class ApplicationActivityRecord {

    private int id;
    private String applicationName;
    private String packageName;
    private long useTime;
    private boolean background;


    /**
     * Create a record, for which the ID is known (already in the database)
     * @param id
     * @param applicationName
     * @param packageName
     * @param useTime
     * @param background
     */
    protected ApplicationActivityRecord(int id, String applicationName, String packageName, long useTime, boolean background){
        this.id = id;
        this.applicationName = applicationName;
        this.packageName = packageName;
        this.useTime = useTime;
        this.background = background;
    }


    /**
     * Create a new record.
     * @param applicationName
     * @param packageName
     * @param useTime
     * @param background
     */
    public ApplicationActivityRecord(String applicationName, String packageName, long useTime, boolean background){
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
