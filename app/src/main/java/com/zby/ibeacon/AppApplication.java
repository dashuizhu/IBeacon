package com.zby.ibeacon;

import android.app.Application;
import android.content.Context;
import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;

/**
 * @author zhuj 2019/4/27 下午10:20.
 */
public class AppApplication extends Application {

    public static DeviceBean sDeviceBean;

    private static AppApplication sAppApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        BleManager.init(this);
        sAppApplication = this;
    }

    public static Context getContext() {
        return sAppApplication;
    }


}
