package com.zby.corelib;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.Context.BIND_AUTO_CREATE;

public class BleManager {

    private final String TAG = "bleManager";

    private BluetoothAdapter   mBluetoothAdapter;
    private BluetoothManager   mBluetoothManager;
    private ServiceConnection  serviceConnection;
    private BluetoothLeService mBluetoothLeService;
    private Context            mContext;

    IConnectInterface mInterface;
    private CmdProcess   mCmdProcess;
    private CmdParseImpl mCmdParse;

    private static volatile BleManager mBleManager;

    private int SCAN_TIME = 10000;

    private OnScanDeviceListener   mDeviceListener;
    private OnDeviceUpdateListener mDeviceUpdateListener;
    private DeviceBean             mDb;
    private List<DeviceBean>       mDeviceList = new ArrayList<>();

    private Thread            scanThread;
    //private Map<String, Long> mMacMap = new HashMap<>();

    /**
     * default AES128 KEY
     */
    public final static String DEFAULT_KEY = "1111111111111111";

    private BleManager() {
    }

    private BleManager(Context context) {
        mContext = context;
    }

    /**
     *
     * @param application
     * @return
     */
    public static BleManager init(Application application) {
        if (mBleManager == null) {
            synchronized (BleManager.class) {
                if (mBleManager == null) {
                    mBleManager = new BleManager(application);
                }
                mBleManager.initManager(application);
            }
        }
        return mBleManager;
    }

    /**
     * get BlemManager
     */
    public static BleManager getInstance() {
        if (mBleManager == null) {
            throw new RuntimeException("please BleManager.init()");
        }
        return mBleManager;
    }

    private void initManager(Context context) {
        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName service) {
                // TODO Auto-generated method stub
                mBluetoothLeService = null;
            }

            @Override
            public void onServiceConnected(ComponentName arg0, IBinder service) {
                // TODO Auto-generated method stub
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    //蓝牙无法初始化
                }
                if (mInterface == null) {
                    mInterface = new BleImpl(mBluetoothLeService);
                }
            }
        };
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mCmdParse = new CmdParseImpl(mContext);
        mCmdProcess = new CmdProcess(mCmdParse);
    }

    public void bluetoothEnable() {
        if (mBluetoothManager == null) {
            mBluetoothManager =
                    (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return;
            }
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                return;
            }
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    /**
     * scan device
     * Stop automatically after 10 seconds
     * @param isStartScan true startScan, false stop Scan
     */
    public void startScan(boolean isStartScan) {
        if (isStartScan) { //开始搜索
            if (scanThread != null) {
                if (scanThread.isAlive()) {
                    scanThread.interrupt();
                }
                scanThread = null;
            }
            scanThread = new Thread(scanRunable);
            if (mBluetoothManager == null) {
                mBluetoothManager =
                        (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
                if (mBluetoothManager == null) {
                    return;
                }
            }

            if (mBluetoothAdapter == null) {
                mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter == null) {
                    return;
                }
            }
            //mMacMap.clear();
            scanThread.start();
        } else { //关闭搜索线程
            if (scanThread != null) {
                scanThread.interrupt();
                scanThread = null;
            }
        }
    }

    Runnable scanRunable = new Runnable() {

        @Override
        public void run() {
            if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.startLeScan(scanCallBack);
            }
            try {
                Thread.sleep(SCAN_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (mBluetoothAdapter != null) {
                    mBluetoothAdapter.stopLeScan(scanCallBack);
                }
                if (mDeviceListener != null) {
                    mDeviceListener.onScanFinsih();
                }
            }
        }
    };

    private BluetoothAdapter.LeScanCallback scanCallBack = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice arg0, int arg1, byte[] arg2) {
            Log.d(TAG, "found device :" + arg0.getAddress() + "  " + arg0.getName());
            //if (TextUtils.isEmpty(arg0.getName()) || !arg0.getName()
            //        .toLowerCase()
            //        .startsWith("un")) {
            //    return;
            //}
            // TODO Auto-generated method stub
            //if (mMacMap.containsKey(arg0.getAddress())) {
            //    //避免一次搜索， 同一个设备多次回调
            //    long delayTime = System.currentTimeMillis() - mMacMap.get(arg0.getAddress());
            //    if (delayTime < 500) {
            //        Log.i(TAG, " filter --- > " + arg0.getAddress());
            //        return;
            //    }
            //}
            int ele = MyByteUtils.byteToInt(arg2[15]);
            int onoff = MyByteUtils.byteToInt(arg2[17]);
            //mMacMap.put(arg0.getAddress(), System.currentTimeMillis());
            foundDevice(arg0, arg1, ele, onoff == 1);
            //}
        }
    };

    /**
     * @param device
     * @param arg1
     */
    private void foundDevice(BluetoothDevice device, int arg1, int ele, boolean onfoo) {
        for (DeviceBean db : mDeviceList) {
            if (db.getMac() == device.getAddress()) {
                db.name = device.getName();
                db.rssi = arg1;
                db.eleType = ele;
                db.onOff = onfoo;
                db.isBonded = device.getBondState() == BluetoothDevice.BOND_BONDED;
                if (mDeviceListener != null) {
                    mDeviceListener.onDeviceFound(db);
                }
                return;
            }
        }
        DeviceBean bean = new DeviceBean();
        bean.name = device.getName();
        bean.mac = device.getAddress();
        bean.rssi = arg1;
        bean.eleType = ele;
        bean.onOff = onfoo;
        bean.isBonded = device.getBondState() == BluetoothDevice.BOND_BONDED;
        mDeviceList.add(bean);
        if (mDeviceList != null) {
            mDeviceListener.onDeviceFound(bean);
        }
    }

    //public boolean isBonded(String mac) {
    //    BluetoothDevice bd = mBluetoothAdapter.getRemoteDevice(mac);
    //    if (bd != null) {
    //        return bd.getBondState() == BluetoothDevice.BOND_BONDED;
    //    }
    //    return false;
    //}

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectAction.ACTION_GATT_CONNECTED);
        intentFilter.addAction(ConnectAction.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ConnectAction.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ConnectAction.ACTION_RECEIVER_DATA);
        return intentFilter;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String mac = intent.getStringExtra(ConnectAction.BROADCAST_DEVICE_MAC);
            if (ConnectAction.ACTION_GATT_CONNECTED.equals(action)) {
                //连接设备
                //mCmdProcess.cleanCache();
            } else if (ConnectAction.ACTION_GATT_DISCONNECTED.equals(action)) {
                //断开连接
                if (mDeviceListener != null) {
                    mDeviceListener.onLostLink(mDb);
                }
            } else if (ConnectAction.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e("ble_test", " 发现服务 " + isConnected(mDb));
                //发现服务
                if (mDeviceListener != null) {
                    mDeviceListener.onLinked(mDb);
                }
            } else if (ConnectAction.ACTION_RECEIVER_DATA.equals(action)) { //解析数据
                byte[] buffer = intent.getByteArrayExtra(ConnectAction.BROADCAST_DATA_value);
                LogUtils.logV("bleManager", "接受数据:" + MyHexUtils.buffer2String(buffer));
                if (mDb != null) {
                    //mCmdProcess.cleanCache();
                    //mCmdProcess.processDataCommand(mDb, buffer, buffer.length);
                    if (mDeviceUpdateListener != null) {
                        mDeviceUpdateListener.onDataUpdate(mDb);
                    }
                }
            }
        }
    };

    /**
     * connect
     */
    public void connect(DeviceBean db) {
        if (db == null) {
            throw new NullPointerException();
        }
        mDb = db;
        mInterface.connect(db.getMac(), "0000");
    }

    /**
     * stopConnect
     */
    public void stopConnect() {
        mInterface.stopConncet();
        mDb = null;
    }

    public boolean isConnecting() {
        return mInterface.isConnecting();
    }

    public boolean isConnected(DeviceBean db) {
        if (db == null) {
            throw new NullPointerException();
        }
        return mInterface.isLink(db.getMac());
    }

    /**
     * destroy
     */
    public void destroy() {
        stopConnect();
        if (scanThread != null) {
            scanThread.interrupt();
            scanThread = null;
        }
        mContext.unregisterReceiver(mGattUpdateReceiver);
        if (mBluetoothLeService != null) {
            mBluetoothLeService.closeAll();
        }
        mContext.unbindService(serviceConnection);
        mContext = null;
    }

    public interface OnScanDeviceListener {
        /**
         * device found
         */
        void onDeviceFound(DeviceBean db);

        /**
         * scan finish;
         */
        void onScanFinsih();

        /**
         * link success
         */
        void onLinked(DeviceBean db);

        /**
         * when the link lost
         */
        void onLostLink(DeviceBean db);
    }

    public void addOnScanDeviceListener(OnScanDeviceListener listener) {
        mDeviceListener = listener;
    }

    public interface OnDeviceUpdateListener {

        /**
         * deviceBean data change
         */
        void onDataUpdate(DeviceBean db);
        void onData(byte[] buff);
    }

    public void addOnDeviceUpdateListener(OnDeviceUpdateListener listener) {
        this.mDeviceUpdateListener = listener;
    }
}
