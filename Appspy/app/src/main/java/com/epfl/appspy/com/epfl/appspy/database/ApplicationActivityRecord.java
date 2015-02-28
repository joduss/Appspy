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


    private int recordId;
    private String packageName;
    private long useTime;
    private boolean background;


    /**
     *
     * @param recordId
     * @param packageName
     * @param useTime
     * @param background
     */
    protected ApplicationActivityRecord(int recordId, String packageName, long useTime, boolean background){
        this.recordId = recordId;
        this.packageName = packageName;
        this.useTime = useTime;
        this.background = background;
    }


    /**
     *
     * @param packageName
     * @param useTime
     * @param background
     */
    public ApplicationActivityRecord(String packageName, long useTime, boolean background){
        this.packageName = packageName;
        this.useTime = useTime;
        this.background = background;
    }




    public int getRecordId() {
        return recordId;
    }


    public void setRecordId(int recordId) {
        this.recordId = recordId;
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
