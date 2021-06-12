package com.zby.ibeacon.ui.ota;

import android.util.Log;
import com.zby.corelib.LogUtils;
import com.zby.corelib.MyHexUtils;
import com.zby.corelib.utils.Crc16Util;

/**
 * @author zhuj 2021/5/29 4:52 PM.
 */
public class OTACmdUtils {

    public static byte[] startOta(String mac) {
        byte[] buff = new byte[18];
        buff[0] = (byte) 0xFF;
        buff[1] = (byte) 0x01;
        String macAdd = mac.replace(":", "");
        byte[] macBuff = MyHexUtils.hexStringToByte(macAdd);
        System.arraycopy(macBuff, 0, buff, 2, macBuff.length);

        //填充剩余内容
        for (int i = 8; i < buff.length; i++) {
            buff[i] = (byte) 0xAA;
        }
        byte[] data = Crc16Util.getData(buff);
        LogUtils.writeLogToFile("otaStart", MyHexUtils.buffer2String(data));

        return data;
    }

    public static byte[] startOtaEnd(int maxCount) {
        byte[] buff = new byte[18];
        buff[0] = (byte) 0xFF;
        buff[1] = (byte) 0x02;
        buff[2] = (byte) (maxCount/ 256);
        buff[3] = (byte) (maxCount%256);
        buff[4] = (byte) ~buff[2];
        buff[5] = (byte) ~buff[3];

        //填充剩余内容
        for (int i = 6; i < buff.length; i++) {
            buff[i] = (byte) 0x55;
        }
        byte[] data = Crc16Util.getData(buff);
        LogUtils.writeLogToFile("otaEnd", MyHexUtils.buffer2String(data));
        return data;
    }

    //public static Observable<byte[]> sendOta(final byte[] buff, final int sleepTime) {
    //    Observable<byte[]> observable = Observable.create(new ObservableOnSubscribe<byte[]>() {
    //        @Override
    //        public void subscribe(ObservableEmitter<byte[]> emitter) throws Exception {
    //            byte[] bu;
    //            int cout = buff.length / AppConstants.PACKAGE_DATA;
    //            int val = buff.length% AppConstants.PACKAGE_DATA;
    //            boolean hasVal = val > 0;
    //            if (hasVal) {
    //                cout += 1;
    //            }
    //
    //            byte[] data;
    //
    //            boolean isLast;
    //            int startIndex = AppConstants.isEncrypt ? 2 : 0;
    //
    //            for (int i = 0; i < cout; i++) {
    //
    //                isLast = (i == cout - 1);
    //                //加密 ，2个字节序号 16字节内容， 2个字节加密
    //                //不加密，就直接 20个字节全内容
    //                bu = new byte[AppConstants.isEncrypt ? 18 : 20];
    //
    //                //最后一包，内容不够，填充FF
    //                if (isLast) {
    //                    for (int j = 0; j < bu.length; j++) {
    //                        bu[j] = (byte) 0xFF;
    //                    }
    //                    System.arraycopy(buff, i * AppConstants.PACKAGE_DATA, bu, startIndex, hasVal ? val : AppConstants.PACKAGE_DATA);
    //
    //                } else {
    //                    System.arraycopy(buff, i * AppConstants.PACKAGE_DATA, bu, startIndex, AppConstants.PACKAGE_DATA);
    //                }
    //
    //                if (AppConstants.isEncrypt) {
    //                    bu[0] = (byte) (i / 256);
    //                    bu[1] = (byte) (i % 256);
    //                    //data = Crc16Util.getData(bu);
    //                    emitter.onNext(bu);
    //                } else {
    //                    emitter.onNext(bu);
    //                }
    //
    //            }
    //            Thread.sleep(1500);
    //            emitter.onComplete();
    //        }
    //    }).doOnNext(new Consumer<byte[]>() {
    //        @Override
    //        public void accept(byte[] bytes) throws Exception {
    //            Thread.sleep(sleepTime);
    //        }
    //    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    //    return observable;
    //}
}
