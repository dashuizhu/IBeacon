package com.zby.ibeacon.ui;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.baseadapter.BGAOnRVItemClickListener;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;
import com.zby.corelib.LogUtils;
import com.zby.corelib.MyHexUtils;
import com.zby.ibeacon.AppApplication;
import com.zby.ibeacon.R;
import com.zby.ibeacon.adapter.DeviceAdapter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeviceListActivity extends AppCompatActivity {

    private final String TAG = "Devicelist";

    @BindView(R.id.recyclerView)  RecyclerView       mRecyclerView;
    @BindView(R.id.refreshLayout) SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.et)            EditText           mEt;
    @BindView(R.id.btn_bind)      Button             mBtnBind;

    private DeviceAdapter mAdapter;

    private List<DeviceBean> mList = new ArrayList<>();

    private boolean mIsBind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        ButterKnife.bind(this);

        initViews();
        BleManager.getInstance().bluetoothEnable();
        mRefreshLayout.setEnableRefresh(false);
        initEdit();
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
        //mAdapter.setOnRVItemClickListener(new BGAOnRVItemClickListener() {
        //    @Override
        //    public void onRVItemClick(ViewGroup parent, View itemView, int position) {
        //
                //Toast.makeText(DeviceListActivity.this, R.string.toast_linking, Toast.LENGTH_LONG)
                //        .show();
                //DeviceBean bd = mAdapter.getData().get(position);
                //BleManager.getInstance().connect(bd);
        //    }
        //});

        BleManager.getInstance().addOnScanDeviceListener(new BleManager.OnScanDeviceListener() {
            @Override
            public synchronized void onDeviceFound(DeviceBean db, byte[] data) {
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
                mBtnBind.setBackgroundColor(mIsBind ? ContextCompat.getColor(DeviceListActivity.this, R.color.color_red)
                        : ContextCompat.getColor(DeviceListActivity.this, R.color.divide_line));
                if (mIsBind) {
                    dispoasbeScan();
                    BleManager.getInstance().startScan(true);
                }
            }
        });

    }

    private void setFilterData() {
        String filterData = mEt.getText().toString();
        if (TextUtils.isEmpty(filterData)) {
            mAdapter.setData(mList);
            return;
        }
        filterData = filterData.toLowerCase();
        List list = new ArrayList();
        for (DeviceBean db : mList) {
            if (db.getMac().toLowerCase().contains(filterData)) {
                list.add(db);
            }
        }
        mAdapter.setData(list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0系统蓝牙搜索需要 location权限
            Disposable dis = new RxPermissions(this).request(Manifest.permission.ACCESS_COARSE_LOCATION,
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
        mStartScan = Observable.interval(12, TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.w(TAG, " scan start");
                        BleManager.getInstance().startScan(true);
                    }
                });
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

}
