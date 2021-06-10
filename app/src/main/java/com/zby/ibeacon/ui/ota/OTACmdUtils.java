package com.zby.ibeacon.ui.ota;

import com.zby.corelib.AppConstants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author zhuj 2021/5/29 4:52 PM.
 */
public class OTACmdUtils {

    public static byte[] startOta() {
        byte[] buff = new byte[2];
        buff[0] = (byte) 0xFF;
        buff[1] = (byte) 0x01;
        return buff;
    }

    public static byte[] startOtaEnd() {
        byte[] buff = new byte[2];
        buff[0] = (byte) 0xFF;
        buff[1] = (byte) 0x02;
        return buff;
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
