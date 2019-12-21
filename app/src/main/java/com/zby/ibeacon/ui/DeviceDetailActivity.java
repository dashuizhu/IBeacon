package com.zby.ibeacon.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;
import com.zby.ibeacon.AppApplication;
import com.zby.ibeacon.R;
import com.zby.ibeacon.adapter.DeviceAdapter;
import com.zby.ibeacon.utils.BeepManager;
import com.zby.ibeacon.utils.ClickUtils;
import com.zby.ibeacon.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceDetailActivity extends AppCompatActivity {

    private final String TAG = DeviceDetailActivity.class.getSimpleName();

    private static final int handle_scanFinish = 101;
    private static final int handle_lostLink   = 102;
    private static final int handle_Link       = 103;
    private static final int handle_alert      = 104;

    @BindView(R.id.iv_back)     ImageView        mIvBack;
    @BindView(R.id.tv_title)    TextView         mTvTitle;
    @BindView(R.id.tv_connect)  TextView         mTvConnect;
    @BindView(R.id.progressBar) ProgressBar      mProgressBar;
    @BindView(R.id.cl_title)    ConstraintLayout mClTitle;
    @BindView(R.id.tv_babySeat) TextView         mTvBabySeat;
    @BindView(R.id.iv_ele)      ImageView        mIvEle;
    @BindView(R.id.iv_warning)  ImageView        mIvWarning;

    private DeviceBean db;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.w(TAG, "onhandle " + msg.what);
            switch (msg.what) {
                case handle_scanFinish:

                    break;
                case handle_lostLink:
                    ToastUtils.toast(DeviceDetailActivity.this, R.string.toast_lostLink);
                    mTvConnect.setVisibility(View.VISIBLE);
                    mTvConnect.setText(R.string.label_connect);
                    mProgressBar.setVisibility(View.GONE);

                    break;
                case handle_Link:
                    initStatus();
                    mTvConnect.setText(R.string.label_disconnect);
                    mTvConnect.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case handle_alert:
                    startAlertAnimation();
                    break;
                default:
            }
        }
    };

    /**
     * 延迟报警时间
     */
    private final int DELAY_TIME = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        ButterKnife.bind(this);

        db = AppApplication.sDeviceBean;

        BleManager.getInstance().addOnDeviceUpdateListener(new BleManager.OnDeviceUpdateListener() {
            @Override
            public void onDataUpdate(DeviceBean deviceBean) {
                initStatus();
                if (deviceBean.isBabySeat()) {
                    mHandler.removeMessages(handle_alert);
                    mHandler.sendEmptyMessageDelayed(handle_alert, DELAY_TIME);
                    Log.w(TAG, "开始5秒延迟 报警");
                } else {
                    mHandler.removeMessages(handle_alert);
                }
            }
        });
        mTvTitle.setText(db.getName());

        BleManager.getInstance().connect(db);
        mTvConnect.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

    }

    BleManager.OnScanDeviceListener mListener = new BleManager.OnScanDeviceListener() {
        @Override
        public synchronized void onDeviceFound(DeviceBean deviceBean) {
            Log.w(TAG, " ondevice _found " + deviceBean.getMac());
//            if (db.isBabySeat()) {

            if (deviceBean.getMac().equals(db.getMac())) {
                mHandler.removeMessages(handle_alert);
                mHandler.sendEmptyMessageDelayed(handle_alert, DELAY_TIME);
                Log.w(TAG, "开始5秒延迟 报警");
            }

//            }
        }

        @Override
        public void onScanFinsih() {
            BleManager.getInstance().startScan(true);
        }

        @Override
        public void onLinked(DeviceBean db) {
            //db.getStatus();
            mHandler.sendEmptyMessage(handle_Link);

            mHandler.removeMessages(handle_alert);
            mHandler.sendEmptyMessageDelayed(handle_alert, DELAY_TIME);
        }

        @Override
        public void onLostLink(DeviceBean db) {
            mHandler.sendEmptyMessage(handle_lostLink);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        BleManager.getInstance().addOnScanDeviceListener(mListener);
    }

    private void initStatus() {
        mTvBabySeat.setText(db.isBabySeat() ? "座椅有人" : "座椅为空");
        mIvEle.setBackgroundResource(db.isEleLow() ? R.mipmap.img_ele_low : R.mipmap.img_ele_full);
    }


    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        BleManager.getInstance().startScan(false);
        BleManager.getInstance().stopConnect();
        stopAlertAnimation();
        super.onDestroy();
    }

    @OnClick({R.id.iv_back, R.id.tv_connect, R.id.iv_warning})
    public void onViewClicked(View view) {
        if (ClickUtils.isFastClick(view.getId())) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_connect:
                mTvConnect.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                if (BleManager.getInstance().isConnected(db)) {
                    BleManager.getInstance().stopConnect();
                } else {
                    BleManager.getInstance().connect(db);
                }
                break;
            case R.id.iv_warning:
                stopAlertAnimation();
                break;
        }
    }

    private BeepManager mBeepManager;

    private void startAlertAnimation() {
        if (mBeepManager == null) {
            mBeepManager = new BeepManager(this);
        }
        mBeepManager.playBeepSoundAndVibrate();
        mIvWarning.setVisibility(View.VISIBLE);
        AnimatorSet animatorSetsuofang = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mIvWarning, "scaleX", 1, 1.5f, 1);//后几个参数是放大的倍数
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mIvWarning, "scaleY", 1, 1.5f, 1);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);//永久循环
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        animatorSetsuofang.setDuration(2000);//时间
        animatorSetsuofang.play(scaleX).with(scaleY);//两个动画同时开始
        animatorSetsuofang.start();//开始
    }

    private void stopAlertAnimation() {
        if (mBeepManager != null) {
            mBeepManager.stop();
        }
        mIvWarning.setVisibility(View.GONE);
        mIvWarning.clearAnimation();
    }


}
