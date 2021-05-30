package com.zby.ibeacon.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;
import com.zby.ibeacon.AppApplication;
import com.zby.ibeacon.R;

/**
 * 锁的 sdk demo详情页
 */
public class DeviceDetailActivity extends AppCompatActivity {

    private DeviceBean db;
    private TextView   mTvDevice;

    private EditText mEtKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        mEtKey = findViewById(R.id.et_key);
        db = AppApplication.sDeviceBean;
        mTvDevice = findViewById(R.id.tv_device);
        mTvDevice.setText(getDevcieJson());
        BleManager.getInstance().addOnDeviceUpdateListener(new BleManager.OnDeviceUpdateListener() {
            @Override
            public void onDataUpdate(DeviceBean db) {
                mTvDevice.setText(getDevcieJson());
            }

            @Override
            public void onData(byte[] buff) {

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
                + " key:" + db.getKey()
                +"  \n点击发送 读取信息";
    }

    public void setNowKey(View view) {
        String str = mEtKey.getText().toString();
        if (TextUtils.isEmpty(str) || str.getBytes().length !=16) {
            Toast.makeText(this, "必须utf8编码 16个字节长度",Toast.LENGTH_LONG).show();
            return;
        }
        db.setKey(str);
        mTvDevice.setText(getDevcieJson());
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
