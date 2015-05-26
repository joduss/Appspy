package com.epfl.appspy.database;

/**
 * Created by Jonathan Duss on 26.02.15.
 *
 * This class represent a record of installation/uninstallation of an app
 */
public class ApplicationInstallationRecord {

    private int appId;
    private String applicationName;
    private String packageName;
    private long installationDate;
    private long uninstallationDate;
    private boolean isSystem;


    /**
     *  @param appId
     * @param applicationName
     * @param packageName
     * @param installationDate
     * @param uninstallationDate
     * @param isSystem
     */
    public ApplicationInstallationRecord(int appId, String applicationName, String packageName, long installationDate,
                                         long uninstallationDate, boolean isSystem) {
        this.appId = appId;
        this.applicationName = applicationName;
        this.packageName = packageName;
        this.installationDate = installationDate;
        this.uninstallationDate = uninstallationDate;
        this.isSystem = isSystem;
    }


    /**
     *  @param applicationName
     * @param packageName
     * @param installationDate
     * @param uninstallationDate
     * @param isSystem
     */
    public ApplicationInstallationRecord(String applicationName, String packageName, long installationDate,
                                         long uninstallationDate, boolean isSystem) {
        this.applicationName = applicationName;
        this.packageName = packageName;
        this.installationDate = installationDate;
        this.uninstallationDate = uninstallationDate;
        this.isSystem = isSystem;
    }


    public int getAppId() {
        return appId;
    }


    public void setAppId(int appId) {
        this.appId = appId;
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


    public long getInstallationDate() {
        return installationDate;
    }


    public void setInstallationDate(long installationDate) {
        this.installationDate = installationDate;
    }


    public long getUninstallationDate() {
        return uninstallationDate;
    }


    public void setUninstallationDate(long uninstallationDate) {
        this.uninstallationDate = uninstallationDate;
    }


    public boolean isSystem() {
        return isSystem;
    }


    public void setSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }
}
