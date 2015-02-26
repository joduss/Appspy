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
    private int appId;
    private long useTime;
    private boolean background;


    /**
     *
     * @param recordId
     * @param appId
     * @param useTime
     * @param background
     */
    protected ApplicationActivityRecord(int recordId, int appId, long useTime, boolean background){
        this.recordId = recordId;
        this.appId = appId;
        this.useTime = useTime;
        this.background = background;
    }


    /**
     *
     * @param appId
     * @param useTime
     * @param background
     */
    public ApplicationActivityRecord(int appId, long useTime, boolean background){
        this.appId = appId;
        this.useTime = useTime;
        this.background = background;
    }




    public int getRecordId() {
        return recordId;
    }


    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }


    public int getAppId() {
        return appId;
    }


    public void setAppId(int appId) {
        this.appId = appId;
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
