package com.zby.corelib;

/**
 * 协议
 */
class CmdPackage {

    /**
     * 识别
     */
    protected final static int TYPE_CHECK       = 0xA1;
    /**
     * 开锁
     */
    private final static int TYPE_OPEN        = 0xA2;
    /**
     * 获取电压，开关
     */
    private final static int TYPE_GET_STATUS  = 0xA3;
    /**
     * 转换加密模式
     */
    private final static int TYPE_CHANGE_MODE = 0xA4;
    /**
     * 设置密钥
     */
    private final static int TYPE_SET_KEY     = 0xA5;

    public static byte[] setCheck(String mac) {
        byte[] buff = new byte[10];
        buff[0] = (byte) TYPE_CHECK;
        byte[] buffer = MyHexUtils.hexStringToByte(mac);
        System.arraycopy(buffer, 0, buff, 1, buffer.length);
        return buff;
    }

    public static byte[] setOpen(String mac, int duration) {
        byte[] buff = new byte[9];
        buff[0] = (byte) TYPE_OPEN;
        byte[] buffer = MyHexUtils.hexStringToByte(mac);
        System.arraycopy(buffer, 0, buff, 1, buffer.length);
        buff[7] = (byte) 0x55;
        buff[8] = (byte) duration;
        return buff;
    }

    public static byte[] setKey(String key) {
        byte[] buffer = key.getBytes();

        byte[] buff = new byte[1+buffer.length];
        buff[0] = (byte) TYPE_SET_KEY;
        System.arraycopy(buffer, 0, buff, 1, buffer.length);
        return buff;
    }

    public static byte[] getStatus() {
        byte[] buff = new byte[2];
        buff[0] = (byte) TYPE_GET_STATUS;
        buff[1] = (byte) 0x01;
        return buff;
    }

    public static byte[] setChangeMode() {
        byte[] buff = new byte[2];
        buff[0] = (byte) TYPE_CHANGE_MODE;
        buff[1] = (byte) 0x02;
        return buff;
    }


}
