package com.zby.ibeacon.ui.ota;

import android.util.Log;
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

    public static Observable<byte[]> sendOta(final byte[] buff, final int sleepTime) {
        Observable<byte[]> observable = Observable.create(new ObservableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(ObservableEmitter<byte[]> emitter) throws Exception {
                byte[] bu;
                int cout = buff.length / 16;
                byte[] data;
                for (int i = 0; i < cout; i++) {
                    bu = new byte[18];
                    //最后一包，内容不够，填充FF
                    if (i == (cout -1)) {
                        for (int j=0; j<bu.length;j++) {
                            bu[j] = (byte) 0xFF;
                        }
                    }

                    bu[0] = (byte) (i >> 8);
                    bu[1] = (byte) (i % 256);

                    System.arraycopy(buff, i * 16, bu, 2, 16);
                    data = Crc16Util.getData(bu);
                    emitter.onNext(data);
                }
                Thread.sleep(2000);
                emitter.onComplete();
            }
        }).doOnNext(new Consumer<byte[]>() {
            @Override
            public void accept(byte[] bytes) throws Exception {
                Thread.sleep(sleepTime);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        return observable;
    }
}
