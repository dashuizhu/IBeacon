package com.zby.corelib;

import android.text.TextUtils;
import android.util.Log;

class BleImpl implements IConnectInterface {

    private final String TAG = BleImpl.class.getSimpleName();

    private BluetoothLeService mService;
    private String             mDeviceAddress;

    private boolean isLink;

    public BleImpl(BluetoothLeService service) {
        this.mService = service;
    }

    @Override
    public boolean connect(String address, String pwd) {
        if (mService.isConnecting(address)) {
            return false;
        }
        isLink = mService.connect(address);
        if (isLink) {
            mDeviceAddress = address;
        }
        return isLink;
    }

    @Override
    public void stopConncet() {
        if (mService != null) {
            mService.disconnect(mDeviceAddress);
        }
    }

    @Override
    public boolean write(byte[] buffer) {
        if (buffer == null) {
            return false;
        }

        return mService.writeLlsAlertLevel(mDeviceAddress, buffer);
    }

    @Override
    public boolean write(byte[] buffer, boolean isNoResponse) {
        if (buffer == null) {
            return false;
        }

        return mService.writeLlsAlertLevel(mDeviceAddress, buffer, isNoResponse);
    }

    @Override
    public boolean writeAgreement(byte[] buffer, String encryptKey) {
        //需要对数据进行abs 128加密
        buffer = CmdEncrypt.sendMessage(buffer);
        if (!TextUtils.isEmpty(encryptKey)) {
            //除了识别命令，其他命令需要 加密
            if (buffer[2] != CmdPackage.TYPE_CHECK) {
                buffer = AESCBCUtil.encrypt(buffer, encryptKey);
                LogUtils.logD("bleimpl", " aes key："+encryptKey +" 加密内容："+MyHexUtils.buffer2String(buffer));
            }
        }
        return write(buffer);
    }

    @Override
    public boolean isLink() {
        if (mService == null) {
            Log.d(TAG, "service is null");
            return false;
        }
        return mService.isLink(mDeviceAddress);
    }

    @Override
    public boolean isLink(String mac) {
        if (mService == null || mac == null) {
            Log.d(TAG, "service is null");
            return false;
        }
        return mService.isLink(mac);
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    public void onBleDestory() {
        // TODO Auto-generated method stub
        mService.close(mDeviceAddress);
    }

    @Override
    public boolean isConnecting() {
        // TODO Auto-generated method stub
        if (mService == null) {
            Log.d(TAG, "service is null");
            return false;
        }
        return mService.isConnecting(mDeviceAddress);
    }

    @Override
    public int getConnectType() {
        return type_ble;
    }

}
