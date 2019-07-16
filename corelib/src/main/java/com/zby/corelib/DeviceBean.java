package com.zby.corelib;

import android.text.TextUtils;

/**
 * DeviceBean
 */
public class DeviceBean {

    String  mac;
    String  name;
    float   voltage;
    int     eleType;
    int     rssi;
    boolean onOff;
    int     duration;
    boolean isBonded;

    String key;

    String cacheKey;

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

    public boolean isOnOff() {
        return onOff;
    }

    /**
     * @return device voltage
     */
    public float getVoltage() {
        return voltage;
    }

    /**
     *
     * @return electricity  100% 70% 30% 0%
     */
    public int getElectricity() {
        int ele =0;
        switch (eleType) {
            case 3:
                ele = 100;
                break;
            case 2:
                ele = 70;
                break;
            case 1:
                ele = 30;
                break;
            default:
                ele = 0;
                break;
        }
        return ele;
    }

    /**
     * @return AES128 encrypt key
     */
    public String getKey() {
        return key;
    }

    /**
     * Send unlock command
     * @return ture is sent successfully
     */
    public boolean sendUnlock() {
        String macStr = mac.replace(":", "");
        return BleManager.getInstance().mInterface.writeAgreement(
                CmdPackage.setOpen(macStr, duration), key);
    }

    /**
     * send read statis command
     * @return  ture is sent successfully
     */
    public boolean sendReadStatus() {
        return BleManager.getInstance().mInterface.writeAgreement(CmdPackage.getStatus(), key);
    }

    /**
     * Send command, modify the key as {@link BleManager#DEFAULT_KEY} after receiving the reply successfully.
     * @return ture is sent successfully
     */
    public boolean sendChangeMode() {
        boolean result =
                BleManager.getInstance().mInterface.writeAgreement(CmdPackage.setChangeMode(), key);
        return result;
    }

    /**
     * Send command, modify the key as newKey after receiving the reply successfully.
     * @param newKey  AES128 key, The key must be 16 bytes long  decoding by UTF-8
     * @return ture is sent successfully
     */
    public boolean sendSetKey(String newKey) {
        if (TextUtils.isEmpty(newKey)) {
            throw new IllegalArgumentException("The key must be 16 bytes long");
        }
        if (newKey.getBytes().length != 16) {
            throw new IllegalArgumentException("The key must be 16 bytes long");
        }
        boolean result =
                BleManager.getInstance().mInterface.writeAgreement(CmdPackage.setKey(newKey), key);
        this.cacheKey = newKey;
        return result;
    }

    /**
     * AEK128 KEY， if input the key null , it will cancel AES128 encryption
     * @param key AES128 key， The key must be 16 bytes long  decoding by UTF-8
     */
    public void setKey(String key) {
        if (!TextUtils.isEmpty(key) && key.getBytes().length != 16) {
            throw new IllegalArgumentException("The key must be null or 16 bytes long");
        }
        this.key = key;
    }

}
