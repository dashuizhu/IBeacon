package com.zby.corelib;

/**
 * DeviceBean
 */
public class DeviceBean {

    String  mac;
    String  name;
    /**
     * 孩子坐姿是否正确
     */
    boolean isBabySeat;
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
        return isBabySeat;
    }

    public boolean isEleLow() {
        return isEleLow;
    }


}
