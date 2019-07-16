package com.zby.corelib;

import android.content.Context;
import android.util.Log;

/**
 * Created by Administrator on 2016/5/13.
 * 解析数据
 */
class CmdParseImpl implements ICmdParseInterface {

    private final static String TAG = CmdParseImpl.class.getSimpleName();

    static final byte TYPE_CHECKID     = (byte) 0xB1;
    static final byte TYPE_OPEN    = (byte) 0xB2;
    static final byte TYPE_STATUS        = (byte) 0xB5;
    static final byte TYPE_CHANGEMODE        = (byte) 0xB6;
    static final byte TYPE_SETKEY     = (byte) 0xB7;

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
        boolean succ;
        switch (dataBuff[0]) {
            case TYPE_CHECKID:
                break;
            case TYPE_OPEN:
                succ = MyByteUtils.byteToInt(dataBuff[1]) == 1;
                db.onOff = succ;
                break;
            case TYPE_STATUS:

                int h = MyByteUtils.byteToInt(dataBuff[1]);
                int l = MyByteUtils.byteToInt(dataBuff[2]);
                //int low = MyByteUtils.byteToInt(dataBuff[3]);

                float vot =  Math.round(740 * (256 * 7 + 128) / 2047) / 100.0f;
                db.voltage = vot;
                db.onOff = MyByteUtils.byteToInt(dataBuff[4]) == 1;

                break;
            case TYPE_CHANGEMODE:
                //默认的key
                db.key = BleManager.DEFAULT_KEY;
                break;
            case TYPE_SETKEY:
                db.key = db.cacheKey;
                break;

            default:
        }
    }
}
