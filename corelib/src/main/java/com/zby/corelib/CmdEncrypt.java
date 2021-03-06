package com.zby.corelib;


class CmdEncrypt {

    private final static String TAG = CmdEncrypt.class.getSimpleName();

    public static byte[] sendMessage(byte[] buff) {
        if (buff == null || buff.length < 2) {
            return null;
        }
        byte[] sendBuff = new byte[20];
        sendBuff[0] = (byte) (buff.length + 1);//长度 ，
        System.arraycopy(buff, 0, sendBuff, 1, buff.length);

        byte checkByte =
                (byte) (sendBuff[0] ^ sendBuff[1] ^ sendBuff[sendBuff.length - 3] ^ sendBuff[
                        sendBuff.length
                                - 2]);

        sendBuff[sendBuff.length - 1] = checkByte;
        LogUtils.logSout("加密:" + MyHexUtils.buffer2String(buff));
        return sendBuff;
    }

    public static byte[] processMessage(byte[] buff) {
        if (buff == null || buff.length < 4) {
            return null;
        }
        LogUtils.logSout("校验：" + MyHexUtils.buffer2String(buff));
        byte checkByte = (byte) (buff[0] ^ buff[1] ^ buff[buff.length - 3] ^ buff[buff.length - 2]);
        //这里硬件校验位错误， 不校验
        //if (checkByte == buff[buff.length - 1]) { //校验位
            //buff[0]是长度， 包括了自己本身， 所以-1
            int length = MyByteUtils.byteToInt(buff[0]);
            byte[] sendBuff = new byte[ length-1];
            System.arraycopy(buff, 1, sendBuff, 0, sendBuff.length);
            return sendBuff;
        //}
        //return null;
    }
}
