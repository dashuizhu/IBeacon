package com.zby.ibeacon.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
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
import com.zby.ibeacon.AppApplication;
import com.zby.ibeacon.R;
import com.zby.ibeacon.adapter.DeviceAdapter;
import java.util.ArrayList;

public class DeviceListActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)  RecyclerView       mRecyclerView;
    @BindView(R.id.refreshLayout) SmartRefreshLayout mRefreshLayout;

    private DeviceAdapter mAdapter;

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
                    Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            mRefreshLayout.autoRefresh();
        }
    }

    private void initViews() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new DeviceAdapter(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                BleManager.getInstance().startScan(true);
                //mRefreshLayout.finishRefresh(10000);
            }
        });

        mAdapter.setData(new ArrayList<DeviceBean>());
        mAdapter.setOnRVItemClickListener(new BGAOnRVItemClickListener() {
            @Override
            public void onRVItemClick(ViewGroup parent, View itemView, int position) {

                Toast.makeText(DeviceListActivity.this, R.string.toast_linking, Toast.LENGTH_LONG)
                        .show();
                DeviceBean bd = mAdapter.getData().get(position);
                BleManager.getInstance().connect(bd);
            }
        });

        BleManager.getInstance().addOnScanDeviceListener(new BleManager.OnScanDeviceListener() {
            @Override
            public synchronized void onDeviceFound(DeviceBean db) {
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
                mRefreshLayout.finishRefresh();
            }

            @Override
            public void onLinked(DeviceBean db) {
                AppApplication.sDeviceBean = db;
                startActivity(new Intent(DeviceListActivity.this, DeviceDetailActivity.class));
                //db.getStatus();
            }

            @Override
            public void onLostLink(DeviceBean db) {
                Toast.makeText(DeviceListActivity.this, R.string.toast_lostLink, Toast.LENGTH_LONG)
                        .show();
                AppApplication.sDeviceBean = null;
            }
        });
    }

    @Override
    protected void onDestroy() {
        //BleManager.getInstance().destroy();
        super.onDestroy();
    }
}
