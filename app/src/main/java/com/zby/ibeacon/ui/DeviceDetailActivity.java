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
        return db.getName()
                + " mac:"
                + db.getMac()
                + "\n major:"
                + db.getMajor()
                + "  minor:"
                + db.getMinor()
                + " broadcastFrequency:"
                + db.getBroadcastFrequency()
                + "\n uuid: "
                + db.getUuid();
    }

    public void getStatus(View view) {
        db.readStatus();
    }

    public void setName(View view) {
        db.setName("name你好" + mRandom.nextInt(10));
    }

    public void setMinor(View view) {
        db.setMinor(mRandom.nextInt(10000));
    }

    public void setMajor(View view) {
        db.setMajor(mRandom.nextInt(10000));
    }

    public void setFrequency(View view) {
        db.setBroadcastFrequency(mRandom.nextInt(10000));
    }

    public void setUUID(View view) {
        db.setUUID("0102030405060708090A0B0C0D0E0F0" + mRandom.nextInt(9));
    }

    public void setCmdRestart(View view) {
        db.setModeRestart();
    }

    public void setCmdDeploy(View view) {
        db.setModeDeploy();
    }

    @Override
    protected void onDestroy() {
        BleManager.getInstance().stopConnect();
        super.onDestroy();
    }
}
