package com.zby.ibeacon.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.baseadapter.BGAOnRVItemClickListener;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zby.corelib.AppConstants;
import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;
import com.zby.corelib.LogUtils;
import com.zby.corelib.MyHexUtils;
import com.zby.corelib.utils.Crc16Util;
import com.zby.ibeacon.R;
import com.zby.ibeacon.adapter.DeviceAdapter;
import com.zby.ibeacon.database.DataBean;
import com.zby.ibeacon.database.DataDao;
import com.zby.ibeacon.utils.SharedPreApp;
import com.zby.ibeacon.utils.ToastUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DeviceListActivity extends AppCompatActivity {

    private final String TAG = "Devicelist";

    @BindView(R.id.recyclerView)  RecyclerView       mRecyclerView;
    @BindView(R.id.refreshLayout) SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.et)            EditText           mEt;
    @BindView(R.id.btn_bind)      Button             mBtnBind;
    @BindView(R.id.tv_device)     TextView           mTvDevice;
    @BindView(R.id.tv_num)        TextView           mTvNum;
    @BindView(R.id.cl)            ConstraintLayout   mCl;
    @BindView(R.id.btn_save)      Button             mBtnSave;
    @BindView(R.id.btn_save_list)      Button             mBtnSaveList;

    private DeviceAdapter mAdapter;

    private List<DeviceBean> mList = new ArrayList<>();

    private boolean mIsBind = false;
    private String mBindMac;

    private MenuItem mMenuItem;

    private DataBean mDataBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        ButterKnife.bind(this);

        initViews();
        BleManager.getInstance().bluetoothEnable();
        mRefreshLayout.setEnableRefresh(false);
        initEdit();
        initDatas();
    }

    private void initViews() {
        //BleManager.getInstance().setEncryptKey("1234567890123456");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new DeviceAdapter(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                BleManager.getInstance().startScan(true);
            }
        });

        mAdapter.setData(new ArrayList<DeviceBean>());
        mAdapter.setOnRVItemClickListener(new BGAOnRVItemClickListener() {
            @Override
            public void onRVItemClick(ViewGroup parent, View itemView, int position) {
                DeviceBean bd = mAdapter.getData().get(position);
                SharedPreApp.getInstance().setBindMac(bd.getMac());
                //SharedPreApp.getInstance().setKeyNum(db.getStepNumber());
                DataDao.saveOrUpdate(bd, bd.getStepNumber());
                initDatas();
            }
        });

        BleManager.getInstance().addOnScanDeviceListener(new BleManager.OnScanDeviceListener() {
            @Override
            public synchronized void onDeviceFound(DeviceBean db, byte[] data) {
                if (TextUtils.equals(db.getMac(), mBindMac)) {
                    mDataBean.setNowStep(db.getStepNumber());
                    mTvNum.setText(String.format("%05d", db.getStepNumber() - mDataBean.getSaveStep()));
                    onNumberChange();
                    Observable.just(db)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<DeviceBean>() {
                                @Override
                                public void accept(DeviceBean deviceBean) throws Exception {
                                    DataDao.saveOrUpdate(deviceBean, deviceBean.getStepNumber());
                                }
                            });
                    //SharedPreApp.getInstance().setKeyNum(db.getStepNumber());
                    return;
                }

                if (mAdapter.getData().contains(db)) {
                    int position = mAdapter.getData().indexOf(db);
                    mAdapter.notifyItemChanged(position);
                    return;
                }
                if (mIsBind) {
                    mList.add(db);
                    setFilterData();
                }
            }

            @Override
            public void onScanFinsih() {
                Log.w(TAG, " scan finish");
                mRefreshLayout.finishRefresh();
            }

            @Override
            public void onLinked(final DeviceBean db) {

            }

            @Override
            public void onLostLink(DeviceBean db) {
                //Toast.makeText(DeviceListActivity.this, R.string.toast_lostLink, Toast.LENGTH_LONG)
                //        .show();
                //AppApplication.sDeviceBean = null;
            }
        });

        initSaveBtn();
    }

    private void initEdit() {
        mEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setFilterData();
            }
        });

        mBtnBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsBind = !mIsBind;
                mBtnBind.setText(mIsBind ? "绑定中..." : "开始绑定");
                mBtnBind.setSelected(mIsBind);
                mBtnBind.setBackgroundColor(
                        mIsBind ? ContextCompat.getColor(DeviceListActivity.this, R.color.color_red)
                                : ContextCompat.getColor(DeviceListActivity.this,
                                        R.color.divide_line));
                if (mIsBind) {
                    dispoasbeScan();
                    BleManager.getInstance().startScan(true);
                }
            }
        });
    }

    private void initDatas() {
        String mac = SharedPreApp.getInstance().getBindMac();
        if (TextUtils.isEmpty(mac)) {
            mCl.setVisibility(View.GONE);
            mRefreshLayout.setVisibility(View.VISIBLE);
            mIsBind = true;
            mBindMac = null;
            mDataBean = null;
        } else {
            mDataBean = DataDao.queryNowData(mac);
            int number = mDataBean.getNowStep() - mDataBean.getSaveStep();

            mRefreshLayout.setVisibility(View.GONE);
            mCl.setVisibility(View.VISIBLE);
            mBindMac = mac;
            mIsBind = false;
            mTvDevice.setText(mBindMac);
            mTvNum.setText(String.format("%05d", number));
        }
        initMenuTitle();
    }

    private void setFilterData() {
        //String filterData = mEt.getText().toString();
        //if (TextUtils.isEmpty(filterData)) {
        //    mAdapter.setData(mList);
        //    return;
        //}
        //filterData = filterData.toLowerCase();
        //List list = new ArrayList();
        //for (DeviceBean db : mList) {
        //    if (db.getMac().toLowerCase().contains(filterData)) {
        //        list.add(db);
        //    }
        //}
        Observable.just("1").observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        mAdapter.setData(mList);
                    }
                });
    }

    private void initSaveBtn() {
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataBean.setSaveStep(mDataBean.getNowStep());
                DataDao.saveClean(mBindMac);
                mTvNum.setText(String.format("%05d", db.getStepNumber() - mDataBean.getSaveStep()));
                onNumberChange();
            }
        });

        mBtnSaveList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DeviceListActivity.this, DeviceDataListActivity.class));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0系统蓝牙搜索需要 location权限
            Disposable dis =
                    new RxPermissions(this).request(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean aBoolean) throws Exception {
                                    if (aBoolean) {
                                        startScan();
                                    }
                                }
                            });
        } else {
            startScan();
        }
    }

    private Disposable mStartScan;

    private void startScan() {
        mStartScan = Observable.interval(1, 10, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                Log.w(TAG, " scan start");
                BleManager.getInstance().startScan(true);
                initDemoData();
            }
        });
    }

    DeviceBean db = new DeviceBean();
    DeviceBean db2 = new DeviceBean();
    int mTestCount = 0;
    private void initDemoData() {
        if (AppConstants.isDemo) {

            db.setMac("00:00:00:00:00:01");
            db2.setMac("00:00:00:00:00:02");

            byte[] buff = MyHexUtils.hexStringToByte("0D FF DA 13 03 01 01 DA 00 00 00 00");

            Random random = new Random();
            if (mDataBean == null) {
                mTestCount = random.nextInt(50);
            } else {
                mTestCount = mDataBean.getNowStep() + random.nextInt(50);
            }
            buff[8] = (byte) ( mTestCount%256);
            buff[9] = (byte) ( mTestCount/256);

            byte[] data = Crc16Util.getData(buff);
            db.setData(data);
            db2.setData(data);
            Log.w(TAG, "demo--- " + MyHexUtils.buffer2String(data));
            Observable.just(data).delay(5, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<byte[]>() {
                        @Override
                        public void accept(byte[] data) throws Exception {
                            BleManager.getInstance().testScan(db, data);
                            BleManager.getInstance().testScan(db2, data);
                        }
                    });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dispoasbeScan();
    }

    private void dispoasbeScan() {
        if (mStartScan != null) {
            if (!mStartScan.isDisposed()) {
                mStartScan.dispose();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mMenuItem = menu.findItem(R.id.action_scan);
        initMenuTitle();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mIsBind) {
            SharedPreApp.getInstance().clear(this);
            initDatas();
            ToastUtils.toast("开始搜索设备");
        } else {
            ToastUtils.toast("正在搜索中...");
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMenuTitle() {
        if (mMenuItem == null) {
            return;
        }
        if (mIsBind) {
            mMenuItem.setTitle("搜索中...");
        } else {
            mMenuItem.setTitle("绑定");
        }
    }

    private void onNumberChange() {
        ValueAnimator animator = ObjectAnimator.ofFloat(mTvNum, "alpha", 1f, 0f, 1f);
        animator.setDuration(1000);
        animator.start();

    }
}
