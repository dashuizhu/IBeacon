package com.zby.corelib;

import android.text.TextUtils;
import io.reactivex.Observable;

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

    byte[] data;

    public DeviceBean() {
    }

    /**
     * @return mac address
     */
    public String getMac() {
        return mac;
    }

    public void setMac(String str) {
        mac = str;
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


    public boolean sendCmdNoResponse(byte[] bytes) {
        return BleManager.getInstance().mInterface.write(bytes, true);
    }

    public void sendCmd(byte[] startOtaEnd) {
        BleManager.getInstance().mInterface.write(startOtaEnd, false);
    }

    public void requestConnectionPriority() {
        BleManager.getInstance().mInterface.requestConnectionPriority();
    }

    public Observable<byte[]> startOta(byte[] by, int otaDataDuration) {
        return BleManager.getInstance().mInterface.startOta(by, otaDataDuration);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceBean) {
            return TextUtils.equals(((DeviceBean) obj).mac.toLowerCase(), this.mac.toLowerCase());
        }
        return false;
    }

    public String getDataString() {
        if (data == null || data.length < 13) {
            return "";
        }
        byte[] buff1 = new byte[4];
        System.arraycopy(data, 8, buff1, 0, 4);
        byte[] buff2 = new byte[2];
        System.arraycopy(data, 12, buff2, 0, 2);
        String s1 = MyHexUtils.buffer2String(buff1);
        String s2 = MyHexUtils.buffer2String(buff2);
        return  s1 + " crc : " + s2;
    }

    public int getStepNumber() {
        if (data == null || data.length < 13) {
            return 0;
        }

        byte[] buff1 = new byte[4];
        System.arraycopy(data, 8, buff1, 0, 4);
        int value = 0;
        for (int i = 3; i>=0; i--) {
            value += buff1[i] * Math.pow(256, i);
        }
        return value;
    }
}
