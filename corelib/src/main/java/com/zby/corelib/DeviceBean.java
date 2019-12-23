package com.zby.corelib;

/**
 * DeviceBean
 */
public class DeviceBean {

    String  mac;
    String  name;
    /**
     * 孩子坐姿是否正确  1有 2false 0为空
     */
    int  babySeatStatus;
    /**
     * 是否低电量
     */
    boolean isEleLow;
    int     rssi;
    boolean isBonded;

    DeviceBean() {

    }

    /**
     * @return mac address
     */
    public String getMac() {
        return mac;
    }

    /**
     * @return device name
     */
    public String getName() {
        return name;
    }

    public boolean isBabySeat() {
        return babySeatStatus == 1;
    }

    public boolean isBabyEmpty() {
        return babySeatStatus == 2;
    }

    public int getBabySeatStatus() {
        return babySeatStatus;
    }

    public void initStatus() {
        this.babySeatStatus = 2;
    }

    public boolean isEleLow() {
        return isEleLow;
    }

}
