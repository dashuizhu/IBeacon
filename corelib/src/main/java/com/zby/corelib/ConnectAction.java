package com.zby.corelib;

class ConnectAction {
    /**
     * 数据传递的key 值
     */
    static final String BROADCAST_DATA_value  = "data";
    static final String BROADCAST_DATA_TYPE   = "type";
    static final String BROADCAST_DEVICE_MAC  = "deviceMac";
    static final String BROADCAST_DEVICE_Name = "name";

    /**
     * 广播事件名, 数据
     */
    static final String ACTION_RECEIVER_DATA = "com.wt.isensor.broadcast";

    /**
     * 连接上设备
     */
    final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";

    /**
     * 连接中
     */
    final static String ACTION_GATT_CONNECTING = "com.example.bluetooth.le.ACTION_GATT_CONNECTING";

    /**
     * 断开连接设备
     */
    final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

    /**
     * BLE 发现service
     */
    final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
}
