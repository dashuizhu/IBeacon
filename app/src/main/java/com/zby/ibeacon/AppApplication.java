package com.zby.ibeacon;

import android.app.Application;
import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;

/**
 * @author zhuj 2019/4/27 下午10:20.
 */
public class AppApplication extends Application {

    public static DeviceBean sDeviceBean;

    @Override
    public void onCreate() {
        super.onCreate();
        BleManager.init(this);
    }




}
