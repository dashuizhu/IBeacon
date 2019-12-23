package com.zby.corelib;

import android.content.Context;
import android.util.Log;

/**
 * Created by Administrator on 2016/5/13.
 * 解析数据
 */
class CmdParseImpl implements ICmdParseInterface {

    private final static String TAG = CmdParseImpl.class.getSimpleName();

    static final byte type_baby_set    = (byte) 0x42;
    static final byte type_ele          = (byte) 0x02;

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
        String data = new String(dataBuff);
        LogUtils.logD(TAG, "解析数据:" + data + strBuffer );
        if (dataBuff.length < 2) {
            return;
        }
        byte[] newBuff;
        String str;
        switch (dataBuff[0]) {
            case type_baby_set:
                if (dataBuff[13] == 0x52) {
                    db.babySeatStatus = 1;
                } else {
                    db.babySeatStatus = 2;
                }

                break;
            case type_ele:
                if (dataBuff[2] == 0x4c) {
                    db.isEleLow = true;
                } else {
                    db.isEleLow = false;
                }
                break;

            default:
        }
    }
}
