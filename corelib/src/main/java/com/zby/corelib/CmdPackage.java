package com.zby.corelib;

/**
 * 协议
 */
class CmdPackage {

    /**
     * 密码认证
     */
    private final static int TYPE_BROADCAST_FREQUENCY = 0xA2;
    private final static int TYPE_MAJOR               = 0xA3;
    private final static int TYPE_MINOR               = 0xA4;
    private final static int TYPE_UUID                = 0xA5;
    private final static int TYPE_NAME                = 0xA7;
    private final static int TYPE_READ_STATUS         = 0xAA;
    private final static int TYPE_CMD                 = 0xAD;

    //public static byte[] setPassword(String password) {
    //    byte[] buff = new byte[18];
    //    buff[0] = (byte) TYPE_PASSWORD;
    //    byte[] buffer = MyHexUtils.hexStringToByte(password);
    //    System.arraycopy(buff, 1, buffer, 0, password.length());
    //    return buff;
    //}

    public static byte[] setName(String name) {
        byte[] buffer = name.getBytes();
        int length = buffer.length > 17 ? 17 : buffer.length;
        byte[] buff = new byte[length+1];
        buff[0] = (byte) TYPE_NAME;
        System.arraycopy(buffer, 0, buff, 1, length);
        return buff;
    }

    public static byte[] setBroadcastFrequency(int time) {
        byte[] buff = new byte[3];
        buff[0] = (byte) TYPE_BROADCAST_FREQUENCY;
        buff[1] = (byte) (time / 256);
        buff[2] = (byte) (time % 256);
        return buff;
    }

    public static byte[] setMajor(int major) {
        byte[] buff = new byte[3];
        buff[0] = (byte) TYPE_MAJOR;
        buff[1] = (byte) (major / 256);
        buff[2] = (byte) (major % 256);
        return buff;
    }

    public static byte[] setMinor(int minor) {
        byte[] buff = new byte[3];
        buff[0] = (byte) TYPE_MINOR;
        buff[1] = (byte) (minor / 256);
        buff[2] = (byte) (minor % 256);
        return buff;
    }

    public static byte[] setUUID(String uuid) {
        byte[] buff = new byte[17];
        buff[0] = (byte) TYPE_UUID;
        byte[] buffer = MyHexUtils.hexStringToByte(uuid);
        System.arraycopy(buffer, 0, buff, 1, buffer.length);
        return buff;
    }

    public static byte[] setCmd(int cmd, int deploy) {
        byte[] buff = new byte[3];
        buff[0] = (byte) TYPE_CMD;
        buff[1] = (byte) cmd;
        buff[2] = (byte) deploy;
        return buff;
    }

    public static byte[] readStatus() {
        byte[] buff = new byte[2];
        buff[0] = (byte) TYPE_READ_STATUS;
        return buff;
    }
}
