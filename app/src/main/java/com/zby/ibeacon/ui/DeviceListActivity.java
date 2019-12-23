package com.zby.ibeacon.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;
import com.zby.ibeacon.AppApplication;
import com.zby.ibeacon.AppConstants;
import com.zby.ibeacon.R;
import com.zby.ibeacon.adapter.DeviceAdapter;
import com.zby.ibeacon.utils.ToastUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.baseadapter.BGAOnRVItemClickListener;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DeviceListActivity extends AppCompatActivity {

    private final int handle_scanFinish = 101;

    @BindView(R.id.recyclerView) RecyclerView     mRecyclerView;
    @BindView(R.id.tv_title)     TextView         mTvTitle;
    @BindView(R.id.tv_connect)   TextView         mTvConnect;
    @BindView(R.id.progressBar)  ProgressBar      mProgressBar;
    @BindView(R.id.cl_title)     ConstraintLayout mClTitle;

    private DeviceAdapter mAdapter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case handle_scanFinish:
                    mTvConnect.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    break;
                default:
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        ButterKnife.bind(this);

        initViews();

        BleManager.getInstance().bluetoothEnable();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0蓝牙搜索需要 location权限
            new RxPermissions(this)
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    if (aBoolean) {
                        BleManager.getInstance().startScan(true);
                        mTvConnect.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                    } else {
                        ToastUtils.toast(DeviceListActivity.this, "请打开蓝牙、定位权限");
                    }
                }
            });
        } else {
            BleManager.getInstance().startScan(true);
            mTvConnect.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new DeviceAdapter(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setData(new ArrayList<DeviceBean>());
        mAdapter.setOnRVItemClickListener(new BGAOnRVItemClickListener() {
            @Override
            public void onRVItemClick(ViewGroup parent, View itemView, int position) {

                Toast.makeText(DeviceListActivity.this, R.string.toast_linking, Toast.LENGTH_LONG)
                        .show();
                DeviceBean bd = mAdapter.getData().get(position);
                AppApplication.sDeviceBean = bd;
                startActivity(new Intent(DeviceListActivity.this, DeviceDetailActivity.class));
            }
        });


    }

    BleManager.OnScanDeviceListener mListener = new BleManager.OnScanDeviceListener() {
        @Override
        public synchronized void onDeviceFound(DeviceBean db) {
            if (db.getName() == null || (!AppConstants.BLUE_NAME.equals(db.getName().toLowerCase().trim()))) {
                return;
            }
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                if (mAdapter.getData().get(i).getMac().equals(db.getMac())) {
                    //mAdapter.getData().set(i, db);
                    //mAdapter.notifyItemChanged(i);
                    return;
                }
            }

            mAdapter.getData().add(db);
            mAdapter.notifyItemInserted(mAdapter.getData().size());
        }

        @Override
        public void onScanFinsih() {
            mHandler.sendEmptyMessage(handle_scanFinish);
        }

        @Override
        public void onLinked(DeviceBean db) {

            //db.getStatus();
        }

        @Override
        public void onLostLink(DeviceBean db) {
//            Toast.makeText(DeviceListActivity.this, R.string.toast_lostLink, Toast.LENGTH_LONG)
//                    .show();
        }
    };

    @Override
    protected void onDestroy() {
        //BleManager.getInstance().destroy();
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @OnClick(R.id.tv_connect)
    public void onViewClicked() {
        mTvConnect.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTvConnect.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        BleManager.getInstance().addOnScanDeviceListener(mListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BleManager.getInstance().startScan(false);
        mHandler.sendEmptyMessage(handle_scanFinish);
    }
}
