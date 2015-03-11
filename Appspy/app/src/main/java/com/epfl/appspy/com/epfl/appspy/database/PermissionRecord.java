package com.epfl.appspy.com.epfl.appspy.database;

/**
 * Created by Jonathan Duss on 08.03.15.
 */
public class PermissionRecord {

    private long id;
    private String packageName;
    private String permissionName;
    private long permissionFirtUse;
    private long permissionLastUse;


    public PermissionRecord(long id, String packageName, String permissionName, long permissionFirtUse, long permissionLastUse){
        this.id = id;
        this.packageName = packageName;
        this.permissionName = permissionName;
        this.permissionFirtUse = permissionFirtUse;
        this.permissionLastUse = permissionLastUse;
    }

    public PermissionRecord(String packageName, String permissionName, long permissionFirtUse, long permissionLastUse){
        this.packageName = packageName;
        this.permissionName = permissionName;
        this.permissionFirtUse = permissionFirtUse;
        this.permissionLastUse = permissionLastUse;
    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
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


    public long getPermissionFirtUse() {
        return permissionFirtUse;
    }


    public void setPermissionFirtUse(long permissionFirtUse) {
        this.permissionFirtUse = permissionFirtUse;
    }


    public long getPermissionLastUse() {
        return permissionLastUse;
    }


    public void setPermissionLastUse(long permissionLastUse) {
        this.permissionLastUse = permissionLastUse;
    }
}
