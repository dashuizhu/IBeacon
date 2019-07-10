package com.zby.corelib;

import android.text.TextUtils;

/**
 * DeviceBean
 */
public class DeviceBean {

    String  mac;
    String  name;
    int     voltage;
    int     power;
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
     * @return device voltage type
     */
    public int getVoltageType() {
        return voltage;
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
