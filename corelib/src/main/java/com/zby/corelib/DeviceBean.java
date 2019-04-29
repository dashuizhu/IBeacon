package com.zby.corelib;

/**
 * DeviceBean
 */
public class DeviceBean {

    String  mac;
    String  name;
    String  uuid;
    int     broadcastFrequency;
    int     major;
    int     minor;
    int     electricity;
    int     rssi;
    int     power;
    boolean onOff;
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

    /**
     * @return BroadcastFrequency 0-65525
     */
    public int getBroadcastFrequency() {
        return broadcastFrequency;
    }

    /**
     * @return uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @return major
     */
    public int getMajor() {
        return major;
    }

    /**
     * @return minor
     */
    public int getMinor() {
        return minor;
    }

    /**
     * @return rssi
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * set name, chart-set "utf-8", name.byte() max length is 17
     */
    public void setName(String name) {
        BleManager.getInstance().mInterface.writeAgreement(CmdPackage.setName(name));
    }

    /**
     * set BroadcastFrequency
     *
     * @param frequency 0-65525
     */
    public void setBroadcastFrequency(int frequency) {
        BleManager.getInstance().mInterface.writeAgreement(
                CmdPackage.setBroadcastFrequency(frequency));
    }

    /**
     * set uuid, 16Byte
     */
    public void setUUID(String uuid) {
        BleManager.getInstance().mInterface.writeAgreement(CmdPackage.setUUID(uuid.toUpperCase()));
    }

    /**
     * set major
     *
     * @param major 0-65525
     */
    public void setMajor(int major) {
        BleManager.getInstance().mInterface.writeAgreement(CmdPackage.setMajor(major));
    }

    /**
     * set minor
     *
     * @param minor 0-65525
     */
    public void setMinor(int minor) {
        BleManager.getInstance().mInterface.writeAgreement(CmdPackage.setMinor(minor));
    }

    /**
     * read status 、name、uuid，
     * when data update callback by {@link BleManager.OnDeviceUpdateListener#onDataUpdate(DeviceBean)}
     */
    public void readStatus() {
        BleManager.getInstance().mInterface.writeAgreement(CmdPackage.readStatus());
    }

    /**
     * restart mode
     */
    public void setModeRestart() {
        BleManager.getInstance().mInterface.writeAgreement(CmdPackage.setCmd(1, 1));
    }

    /**
     * deploy mode
     */
    public void setModeDeploy() {
        BleManager.getInstance().mInterface.writeAgreement(CmdPackage.setCmd(1, 0));
    }
}
