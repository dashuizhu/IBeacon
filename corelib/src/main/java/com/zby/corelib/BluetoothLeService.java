/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zby.corelib;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private boolean isReconnect = true;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private Map<String, BluetoothGatt> gattMaps         = new HashMap<String, BluetoothGatt>();
    private Map<String, BluetoothGatt> gattMapsConnting = new HashMap<String, BluetoothGatt>();

    // private String mBluetoothDeviceAddress;
    // private BluetoothGatt mBluetoothGatt;
    // private int mConnectionState = STATE_DISCONNECTED;

    //public static final int STATE_DISCONNECTED = 0;
    //public static final int STATE_CONNECTING   = 1;
    //public static final int STATE_CONNECTED    = 2;

    private static final UUID SEND_SERVIE_UUID         =
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static final UUID SEND_CHARACTERISTIC_UUID =
            UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");

    private static final UUID RECEIVER_SERVICE        =
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static final UUID RECEIVER_CHARACTERISTIC =
            UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice mBluetoothDevice;

    private MyHandler mHandler;

    // Implements callback methods for GATT events that the app cares about. For
    // example,
    // connection change and services discovered.
    @SuppressLint("NewApi") private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    String intentAction;
                    String address = gatt.getDevice().getAddress();
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        isReconnect = true;
                        intentAction = ConnectAction.ACTION_GATT_CONNECTED;
                        removeGatt(gattMapsConnting, address);
                        addGatt(gattMaps, address, gatt);
                        broadcastUpdate(intentAction, address);
                        LogUtils.logI(TAG, "Connected to GATT server.");
                        // Attempts to discover services after successful connection.
                        LogUtils.logI(TAG,
                                "Attempting to start service discovery:" + gatt.discoverServices());

                        mHandler.sendEmptyMessageDelayed(handler_read_rssi, 2000);

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        // if(!isReconnect) {
                        intentAction = ConnectAction.ACTION_GATT_DISCONNECTED;
                        LogUtils.logI(TAG, "Disconnected from GATT server.");
                        close(address);
                        broadcastUpdate(intentAction, address);
                        // } else {
                        // intentAction = ACTION_GATT_RECONNECTING;
                        // gattMapsConnting.put(address, gatt);
                        // gattMaps.remove(address);
                        // broadcastUpdate(intentAction, address);
                        // gatt.connect();
                        // isReconnect = false;
                        // }
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        String address = gatt.getDevice().getAddress();
                        broadcastUpdate(ConnectAction.ACTION_GATT_SERVICES_DISCOVERED, address);
                        setReceiver(gatt);
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic, int status) {
                    Log.i(TAG, "onCharacteristicRead");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ConnectAction.ACTION_RECEIVER_DATA, characteristic);
                    }
                }

                /**
                 * 返回数据。
                 */
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    broadcastUpdate(ConnectAction.ACTION_RECEIVER_DATA, characteristic);
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic, int status) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "发送回调  " + status + " ");
                    super.onCharacteristicWrite(gatt, characteristic, status);
                }

                @Override
                public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "发送回调  " + status + " ");
                    super.onReliableWriteCompleted(gatt, status);
                }
            };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, String mac) {
        final Intent intent = new Intent(action);
        intent.putExtra("mac", mac);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        final byte[] data = characteristic.getValue();
        //String ss = MyHexUtils.buffer2String(data);
        // if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
        // int flag = characteristic.getProperties();
        // int format = -1;
        // if ((flag & 0x01) != 0) {
        // format = BluetoothGattCharacteristic.FORMAT_UINT16;
        // Log.d(TAG, "Heart rate format UINT16.");
        // } else {
        // format = BluetoothGattCharacteristic.FORMAT_UINT8;
        // Log.d(TAG, "Heart rate format UINT8.");
        // }
        // final int heartRate = characteristic.getIntValue(format, 1);
        // Log.d(TAG, String.format("Received heart rate: %d", heartRate));
        // intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        // } else {
        // For all other profiles, writes the data formatted in HEX.
        if (data != null && data.length > 0) {
            // final StringBuilder stringBuilder = new
            // StringBuilder(data.length);
            // for(byte byteChar : data)
            // stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(ConnectAction.BROADCAST_DATA_value, data);
        }
        // }
        sendBroadcast(intent);
    }

    class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        mHandler = new MyHandler(this);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular
        // example, close() is
        // invoked when the UI is disconnected from the Service.
        closeAll();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
     * callback.
     */
    boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        // if (mBluetoothDeviceAddress != null &&
        // address.equals(mBluetoothDeviceAddress)
        // && mBluetoothGatt != null) {
        // Log.d(TAG,
        // "Trying to use an existing mBluetoothGatt for connection.");
        // if (mBluetoothGatt.connect()) {
        // mConnectionState = STATE_CONNECTING;
        // return true;
        // } else {
        // return false;
        // }
        // }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mBluetoothDevice = device;
        BluetoothGatt ga = getGatt(gattMapsConnting, address);
        if (ga != null) {
            ga.disconnect();
            ga.close();
            removeGatt(gattMapsConnting, address);
        }
        BluetoothGatt gatt = getGatt(gattMaps, address);
        if (gatt != null) {
            gatt.close();
            removeGatt(gattMaps, address);
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        BluetoothGatt mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        synchronized (gattMapsConnting) {
            gattMapsConnting.put(address, mBluetoothGatt);
        }
        Log.d(TAG, "Trying to create a new connection.");
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
     * callback.
     */
    void disconnect(String address) {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGatt mBluetoothGatt = getGatt(gattMaps, address);
        if (mBluetoothGatt != null) {
            removeGatt(gattMaps, address);
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        BluetoothGatt mGatt = getGatt(gattMapsConnting, address);
        if (mGatt != null) {
            removeGatt(gattMapsConnting, address);
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    void close(String address) {
        mHandler.removeCallbacksAndMessages(null);
        BluetoothGatt mBluetoothGatt = getGatt(gattMaps, address);
        if (mBluetoothGatt != null) {
            gattMaps.remove(address);
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mBluetoothGatt = getGatt(gattMapsConnting, address);
        if (mBluetoothGatt != null) {
            gattMapsConnting.remove(address);
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    void closeAll() {
        mHandler.removeMessages(handler_read_rssi);
        synchronized (gattMapsConnting) {
            for (String key : gattMapsConnting.keySet()) {
                BluetoothGatt gatt = gattMapsConnting.get(key);
                if (gatt != null) {
                    gatt.disconnect();
                    gatt.close();
                    gattMapsConnting.remove(key);
                }
            }
        }
        synchronized (gattMaps) {
            for (String key : gattMaps.keySet()) {
                BluetoothGatt gatt = gattMaps.get(key);
                if (gatt != null) {
                    gatt.disconnect();
                    gatt.close();
                    gattMaps.remove(key);
                }
            }
        }
        // mBluetoothAdapter.disable();
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt,
     * android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    void readCharacteristic(String address, BluetoothGattCharacteristic characteristic) {

        BluetoothGatt mBluetoothGatt;
        mBluetoothGatt = getGatt(gattMaps, address);
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    boolean setCharacteristicNotification(BluetoothGatt mBluetoothGatt,
                                          BluetoothGattCharacteristic characteristic, boolean enabled) {
        //BluetoothGatt mBluetoothGatt = null;
        //mBluetoothGatt = getGatt(gattMaps, address);
        if (mBluetoothGatt != null) {
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return false;
            }
            boolean isEnable =
                    mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

            LogUtils.logD(TAG,
                    characteristic.getUuid().toString() + isEnable);
//            return isEnable && isEnable2;

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            boolean setValue = false;
            boolean writeDesc1 = false;
            if (descriptor != null) {
                setValue = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                writeDesc1 = mBluetoothGatt.writeDescriptor(descriptor);
                LogUtils.logD(TAG, " setreceiver  " + setValue + " " + writeDesc1);
            }
            return writeDesc1;

//            BluetoothGattDescriptor descriptor2 = characteristic2.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//            boolean setValue2 = false;
//            boolean writeDesc2 = false;
//            if (descriptor2 != null) {
//                setValue2 = descriptor2.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                writeDesc2 = mBluetoothGatt.writeDescriptor(descriptor2);
//                LogUtils.logD(TAG,  " setreceiver2  " + setValue + " " + writeDesc2);
//
//            }
//            return writeDesc1 && writeDesc2;
        }
        return false;
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    List<BluetoothGattService> getSupportedGattServices(String address) {
        synchronized (gattMaps) {
            if (gattMaps.containsKey(address)) {
                BluetoothGatt mBluetoothGatt = getGatt(gattMaps, address);
                if (mBluetoothGatt == null)
                    return null;
                return mBluetoothGatt.getServices();
            }
            return null;
        }
    }

    BluetoothGattCharacteristic alertLevel = null;

    boolean writeLlsAlertLevel(String address, byte[] bb) {
        BluetoothGatt mBluetoothGatt;
        BluetoothGattService linkLossService;
        synchronized (gattMaps) {
            if (!gattMaps.containsKey(address)) {
                Log.e(TAG, "gatt is null " + address + " ");
                return false;
            }
            mBluetoothGatt = getGatt(gattMaps, address);
            linkLossService = mBluetoothGatt.getService(SEND_SERVIE_UUID);
        }
        if (linkLossService == null) {
            showMessage("link loss Alert service not found!" + mBluetoothGatt.discoverServices());
            // showToast("没有服务");
            return false;
        }

        boolean status = false;
        if (alertLevel == null) {
            alertLevel = linkLossService.getCharacteristic(SEND_CHARACTERISTIC_UUID);
            if (alertLevel == null) {
                showMessage("link loss Alert Level charateristic not found!");
                // showToast("没有特征");
                return false;
            }
            int storedLevel = alertLevel.getWriteType();
            Log.d(TAG, "storedLevel() - storedLevel=" + storedLevel);
            alertLevel.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }

        // enableBattNoti(iDevice);
        alertLevel.setValue(bb);
        status = mBluetoothGatt.writeCharacteristic(alertLevel);
        LogUtils.logV("tag_send", "发送  " + status + " " + MyHexUtils.buffer2String(bb));
        return status;
    }

    //public boolean writeLlsAlertLevelWait(String address, byte[] bb) {
    //    if (!gattMaps.containsKey(address)) {
    //        Log.e(TAG, "gatt is null " + address);
    //        return false;
    //    }
    //    BluetoothGatt mBluetoothGatt = gattMaps.get(address);
    //    // Log.i("iDevice", iDevice);
    //    BluetoothGattService linkLossService = mBluetoothGatt.getService(SEND_SERVIE_UUID);
    //    if (linkLossService == null) {
    //        showMessage(address
    //                + " link loss Alert service not found!  close  "
    //                + mBluetoothGatt.getServices().size()
    //                + " ");
    //        //close(address);
    //        return false;
    //    }
    //    BluetoothGattCharacteristic alertLevel = null;
    //    boolean status = false;
    //    alertLevel = linkLossService.getCharacteristic(SEND_CHARACTERISTIC_UUID);
    //    if (alertLevel == null) {
    //        showMessage("link loss Alert Level charateristic not found!");
    //        return false;
    //    }
    //    int storedLevel = alertLevel.getWriteType();
    //    Log.d(TAG, "storedLevel() - storedLevel=" + storedLevel);
    //
    //    alertLevel.setValue(bb);
    //
    //    alertLevel.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    //    status = mBluetoothGatt.writeCharacteristic(alertLevel);
    //    return status;
    //}

    public int getConnectStatus() {
        if (mBluetoothDevice == null) {
            return 0;
        }
        return mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothProfile.GATT);
    }

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }

    /**
     * 设置回收发的服务
     */
    void setReceiver(final BluetoothGatt mBluetoothGatt) {
        //BluetoothGatt mBluetoothGatt;
        //mBluetoothGatt = getGatt(gattMaps, address);
        if (mBluetoothGatt == null) {
            return;
        }
        BluetoothGattService linkLossService = mBluetoothGatt.getService(RECEIVER_SERVICE);
        if (linkLossService == null)
            return;

        final BluetoothGattCharacteristic characteristic =
                linkLossService.getCharacteristic(RECEIVER_CHARACTERISTIC);
        if (characteristic == null) {
            return;
        }

        final BluetoothGattCharacteristic characteristic2 =
                linkLossService.getCharacteristic(SEND_CHARACTERISTIC_UUID);

        //没有设置成功，就重设监听
        boolean notify2 = setCharacteristicNotification(mBluetoothGatt, characteristic2, true);
        //2个监听需要间隔
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean notify1 = setCharacteristicNotification(mBluetoothGatt, characteristic, true);
            }
        }).start();
//        boolean notify1 = setCharacteristicNotification(mBluetoothGatt, characteristic, true);
        //if (! (notify1 && notify2)) {
        //    Message msg = mHandler.obtainMessage();
        //    msg.what = handler_set_notify1;
        //    msg.obj = mBluetoothGatt.getDevice().getAddress();
        //    mHandler.sendMessageDelayed(msg, 300);
        //}
    }

    private BluetoothGatt getGatt(Map<String, BluetoothGatt> map, String address) {
        synchronized (map) {
            if (map.containsKey(address)) {
                return map.get(address);
            }
            return null;
        }
    }

    private void removeGatt(Map<String, BluetoothGatt> map, String address) {
        synchronized (map) {
            map.remove(address);
        }
    }

    private void addGatt(Map<String, BluetoothGatt> map, String address, BluetoothGatt gatt) {
        synchronized (map) {
            map.put(address, gatt);
        }
    }

    /**
     * 是否正在连接中
     */
    boolean isConnecting(String address) {
        synchronized (gattMapsConnting) {
            return gattMapsConnting.containsKey(address);
        }
    }

    boolean isLink(String address) {
        synchronized (gattMaps) {
            return gattMaps.containsKey(address);
        }
    }

    private Toast mToast;

    private void showToast(String str) {
        if (mToast == null) {
            mToast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        }
        mToast.setText(str);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.show();
    }

    static final int handler_set_notify1 = 103;
    static final int handler_set_notify2 = 103;
    static final int handler_read_rssi = 101;


    private static class MyHandler extends Handler {
        private final WeakReference<BluetoothLeService> mActivity;

        public MyHandler(BluetoothLeService context) {
            mActivity = new WeakReference<BluetoothLeService>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtils.logD(TAG, "onHandlerMessage " + msg.what);
            BluetoothLeService service = mActivity.get();
            if (service != null) {

                BluetoothGatt gatt;
                String address;
                switch (msg.what) {
                    case BluetoothLeService.handler_read_rssi:
                        LogUtils.logD(TAG, "query connect "+service.getConnectStatus());
                        sendEmptyMessageDelayed(handler_read_rssi, 2000);
                        break;

                    case BluetoothLeService.handler_set_notify1:
                        address = (String) msg.obj;
                        gatt = service.gattMaps.get(address);
                        if (gatt != null && gatt.connect()) {
                            service.setReceiver(gatt);
                        }
                        break;
                    default:
                }

            }
        }
    }
}