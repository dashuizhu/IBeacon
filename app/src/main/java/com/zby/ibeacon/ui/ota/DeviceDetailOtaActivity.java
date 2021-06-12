package com.zby.ibeacon.ui.ota;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zby.corelib.AppConstants;
import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;
import com.zby.corelib.LogUtils;
import com.zby.ibeacon.AppApplication;
import com.zby.ibeacon.R;
import com.zby.ibeacon.utils.ToastUtils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * 书海的OTA详情页
 */
public class DeviceDetailOtaActivity extends AppCompatActivity {

    private DeviceBean db;
    private TextView   mTvDevice;
    private TextView   mTvOtaPath;
    private EditText   mEtDuration;
    private Button     mBtnOta;
    private CheckBox   mCb;

    private Uri  mOtaUri;
    private int  mOtaDataDuration;
    private long mTotalData;
    private long mNowData;

    private Disposable mStartDis;
    private Disposable mOtaDis;

    private boolean isOtaIng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail_ota);

        if (AppConstants.isDemo) {
            db = new DeviceBean();
            db.setMac("01:23:45:67:89:AB");
            BleManager.getInstance().connect(db);
        } else {
            db = AppApplication.sDeviceBean;
        }
        mTvDevice = findViewById(R.id.tv_device);
        mTvOtaPath = findViewById(R.id.tv_otapath);
        mEtDuration = findViewById(R.id.et_duration);
        mBtnOta = findViewById(R.id.btn_ota);
        mCb = findViewById(R.id.cb);

        mCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppConstants.setEncrypt(isChecked);
            }
        });

        BleManager.getInstance().addOnDeviceUpdateListener(new BleManager.OnDeviceUpdateListener() {
            @Override
            public void onDataUpdate(DeviceBean db) {
                //sendOtaData();
            }

            @Override
            public void onData(byte[] buff) {

                switch (buff[0]) {
                    case (byte) 0xFF:
                        if (buff[1] == (byte) 0x01) {
                            disDispoable(mStartDis);
                            sendOtaData();
                        } else if (buff[1] == (byte) 0x02) {

                        }
                        break;
                    case 0x55:
                        ToastUtils.toast("OTA失败");
                        mBtnOta.setText("OTA失败");
                        mBtnOta.setEnabled(true);
                        break;
                    default:
                }
            }
        });
    }

    public void clickSelectOtaPath(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0系统蓝牙搜索需要 location权限
            Disposable dis =
                    new RxPermissions(this).request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean aBoolean) throws Exception {
                                    if (aBoolean) {
                                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                                        intent.setType("*/*");
                                        DeviceDetailOtaActivity.this.startActivityForResult(intent,
                                                102);
                                    } else {
                                        ToastUtils.toast("请先允许存储权限");
                                    }
                                }
                            });
        } else {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            DeviceDetailOtaActivity.this.startActivityForResult(intent,
                    102);
        }
    }

    public void startOta(View view) {
        //db.sendReadStatus();
        startOta(mOtaUri);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        mOtaUri = data.getData();
        mTvOtaPath.setText("路径：" + data.getData());
    }

    @Override
    protected void onDestroy() {
        BleManager.getInstance().stopConnect();
        disDispoable(mStartDis);
        disDispoable(mOtaDis);
        super.onDestroy();
    }

    /**
     * 开始根据bin uri升级
     */
    private void startOta(Uri uri) {
        mOtaDataDuration = 0;
        try {
            mOtaDataDuration = Integer.valueOf(mEtDuration.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mOtaDataDuration <= 0) {
            Toast.makeText(this, "时间间隔错误", Toast.LENGTH_LONG).show();
            return;
        }

        if (mOtaUri == null) {
            Toast.makeText(this, "选择ota文件", Toast.LENGTH_LONG).show();
            return;
        }
        mBtnOta.setText("开始...");
        mBtnOta.setEnabled(false);
        db.sendCmd(OTACmdUtils.startOta(db.getMac()));

        if (AppConstants.isDemo) {
            Intent intent = new Intent("com.wt.isensor.broadcast");
            byte[] data = new byte[2];
            data[0] = (byte) 0xFF;
            data[1] = (byte) 0x01;
            intent.putExtra("data", data);
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
        }

        mStartDis = Observable.timer(8, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        //sendOtaData();
                        ToastUtils.toast("开始升级应答超时");
                        mBtnOta.setText("开始");
                        mBtnOta.setEnabled(true);
                    }
                });
    }

    private void sendOtaData() {
        if (isOtaIng) {
            return;
        }
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(mOtaUri);
            byte[] by = new byte[is.available()];//此数字不唯一哦；

            int len;
            while ((len = is.read(by)) != -1) {
                //len就是得出的字节流了。
            }

            mTotalData = by.length;
            mNowData = 0;
            db.requestConnectionPriority();
            isOtaIng = true;



            //OTACmdUtils.sendOta(by, mOtaDataDuration)
            db.startOta(by, mOtaDataDuration).subscribe(new Observer<byte[]>() {
                @Override
                public void onSubscribe(Disposable d) {
                    mOtaDis = d;
                }

                int count =0;
                @Override
                public void onNext(byte[] bytes) {
                    //boolean sendSucc = db.sendCmdNoResponse(bytes);
                    //if (sendSucc) {
                        count += 1;
                        //LogUtils.writeLogToFile("send succ",  + count + " totalsize " + mTotalData);
                    //}

                    mNowData += AppConstants.PACKAGE_DATA;

                    if (mNowData >= mTotalData) {
                        mNowData = mTotalData;
                    }
                    mBtnOta.setText(mNowData + "/" + mTotalData);
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    mBtnOta.setEnabled(true);
                    isOtaIng = false;
                    mBtnOta.setText("升级失败");
                    LogUtils.writeLogToFile("error", e.getMessage());
                }

                @Override
                public void onComplete() {
                    db.sendCmd(OTACmdUtils.startOtaEnd(AppConstants.getPackageMaxIndex(mTotalData)));
                    mBtnOta.setEnabled(true);
                    isOtaIng = false;
                    mBtnOta.setText("完成");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void disDispoable(Disposable dis) {
        if (dis != null) {
            if (!dis.isDisposed()) {
                dis.dispose();
            }
            dis = null;
        }
    }
}
