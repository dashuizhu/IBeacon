package com.zby.corelib;

import android.content.Context;
import android.util.Log;

/**
 * Created by Administrator on 2016/5/13.
 * 解析数据
 */
class CmdParseImpl implements ICmdParseInterface {

    private final static String TAG = CmdParseImpl.class.getSimpleName();

    static final byte type_password     = (byte) 0xB1;
    static final byte type_frequency    = (byte) 0xB2;
    static final byte type_major        = (byte) 0xB3;
    static final byte type_minor        = (byte) 0xB4;
    static final byte type_uuid_set     = (byte) 0xB5;
    static final byte type_password_set = (byte) 0xB6;
    static final byte type_name         = (byte) 0xB7;
    static final byte type_rate         = (byte) 0xB8;
    static final byte type_light        = (byte) 0xB9;
    static final byte type_status       = (byte) 0xBA;
    static final byte type_name_read    = (byte) 0xBB;
    static final byte type_uuid_red     = (byte) 0xBC;
    static final byte type_cmd          = (byte) 0xBD;

    private Context mContext;

    CmdParseImpl(Context context) {
        this.mContext = context;
    }

    @Override
    public void parseData(DeviceBean db, byte[] dataBuff) {
        if (dataBuff == null) {
            return;
        }
        String strBuffer = MyHexUtils.buffer2String(dataBuff);
        LogUtils.logD(TAG, "解析数据:" + strBuffer);
        if (dataBuff.length < 2) {
            return;
        }
        byte[] newBuff;
        String str;
        switch (dataBuff[0]) {
            case type_status:
                db.broadcastFrequency =
                        MyByteUtils.byteToInt(dataBuff[1]) * 256 + MyByteUtils.byteToInt(
                                dataBuff[2]);
                int a = MyByteUtils.byteToInt(dataBuff[3]);
                int a1 = MyByteUtils.byteToInt(dataBuff[4]);
                db.major = a + a1;
                db.minor = MyByteUtils.byteToInt(dataBuff[5]) * 256 + MyByteUtils.byteToInt(
                        dataBuff[6]);
                db.power = MyByteUtils.byteToInt(dataBuff[7]) * 256 + MyByteUtils.byteToInt(
                        dataBuff[8]);
                db.onOff = MyByteUtils.byteToInt(dataBuff[9]) == 1;
                //db.electricity = MyByteUtils.byteToInt(dataBuff[10]);

                break;
            case type_name_read:
                newBuff = new byte[dataBuff.length - 1];
                System.arraycopy(dataBuff, 1, newBuff, 0, newBuff.length);
                str = new String(newBuff);
                db.name = str;
                break;
            case type_uuid_red:
                newBuff = new byte[dataBuff.length - 1];
                System.arraycopy(dataBuff, 1, newBuff, 0, newBuff.length);
                str = MyHexUtils.buffer2String(newBuff);
                //str = new String(newBuff);
                db.uuid = str.toUpperCase();
                break;
            default:
        }
    }
}
