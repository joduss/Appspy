package com.epfl.appspy.old;

/**
 * Created by Jonathan Duss on 11.04.15.
 */
public class CPUInfo {

    private int pid;
    public double averageCpuUsage;
    public int maxCpuUsage;


    public CPUInfo(int pid) {
        this.pid = pid;
        averageCpuUsage = 0;
        maxCpuUsage = 0;
    }


    public int getPid() {
        return pid;
    }
}
