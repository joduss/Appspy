package com.epfl.appspy.database;

/**
 * Created by Jonathan Duss on 08.03.15.
 *
 * This class represents a record for one permission, at a given time for a given application
 */
public class PermissionRecord {

    private long id;
    private String packageName;
    private String permissionName;
    private long timestamp = 0;
    private long permissionFirstUse = 0;
    private long permissionLastUse = 0;


    public PermissionRecord(long id, String packageName, String permissionName, long permissionFirstUse, long permissionLastUse){
        this.id = id;
        this.packageName = packageName;
        this.permissionName = permissionName;
        this.permissionFirstUse = permissionFirstUse;
        this.permissionLastUse = permissionLastUse;
    }


    public PermissionRecord(String packageName, String permissionName, long timestamp){
        this.packageName = packageName;
        this.permissionName = permissionName;
        this.timestamp = timestamp;
        //this.permissionLastUse = permissionLastUse;
    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public long getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public String getPackageName() {
        return packageName;
    }


    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public String getPermissionName() {
        return permissionName;
    }


    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }


    public long getPermissionFirstUse() {
        return permissionFirstUse;
    }


    public void setPermissionFirstUse(long permissionFirtUse) {
        this.permissionFirstUse = permissionFirtUse;
    }


    public long getPermissionLastUse() {
        return permissionLastUse;
    }


    public void setPermissionLastUse(long permissionLastUse) {
        this.permissionLastUse = permissionLastUse;
    }
}
