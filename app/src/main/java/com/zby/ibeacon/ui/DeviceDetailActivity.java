package com.zby.ibeacon.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;
import com.zby.ibeacon.AppApplication;
import com.zby.ibeacon.R;

public class DeviceDetailActivity extends AppCompatActivity {

    private DeviceBean db;
    private TextView   mTvDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        db = AppApplication.sDeviceBean;
        mTvDevice = findViewById(R.id.tv_device);
        mTvDevice.setText(getDevcieJson());
        BleManager.getInstance().addOnDeviceUpdateListener(new BleManager.OnDeviceUpdateListener() {
            @Override
            public void onDataUpdate(DeviceBean db) {
                mTvDevice.setText(getDevcieJson());
            }
        });
    }

    private String getDevcieJson() {
        if (db == null) {
            return null;
        }
        return db.getName()
                + " mac:"
                + db.getMac()
                + " switch："+db.isOnOff()
                + " ele:" + db.getElectricity()
                + " voltage:" + db.getVoltage()
                + " key:" + db.getKey();
    }

    public void readStatus(View view) {
        db.sendReadStatus();
    }

    public void setUnlock(View view) {
        db.sendUnlock();
    }

    public void setChangeMode(View view) {
        db.sendChangeMode();
    }

    public void setKey(View view) {
        db.sendSetKey("1234567890111111");
    }

    @Override
    protected void onDestroy() {
        BleManager.getInstance().stopConnect();
        super.onDestroy();
    }
}
