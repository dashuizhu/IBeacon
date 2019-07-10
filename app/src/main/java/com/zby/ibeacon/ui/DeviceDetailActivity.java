package com.zby.ibeacon.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;
import com.zby.ibeacon.AppApplication;
import com.zby.ibeacon.R;
import java.util.Random;

public class DeviceDetailActivity extends AppCompatActivity {

    private DeviceBean db;
    private TextView   mTvDevice;

    private Random mRandom = new Random();

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
                + " 开关："+db.isOnOff()
                + " ele:" + db.getVoltageType()
                + " key:" + db.getKey();
    }

    public void getStatus(View view) {
        db.sendReadStatus();
    }

    public void setName(View view) {
        //db.sendCheckId();
    }

    public void setMinor(View view) {
        db.sendUnlock();
    }

    public void setMajor(View view) {
        db.sendChangeMode();
    }

    public void setUUID(View view) {
        db.sendSetKey("1234567890111111");
    }



    @Override
    protected void onDestroy() {
        BleManager.getInstance().stopConnect();
        super.onDestroy();
    }
}
