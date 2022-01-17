package com.zby.ibeacon.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.baseadapter.BGAAdapterViewAdapter;
import cn.bingoogolapple.baseadapter.BGAOnRVItemClickListener;
import cn.bingoogolapple.baseadapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.baseadapter.BGAViewHolderHelper;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zby.corelib.AppConstants;
import com.zby.corelib.BleManager;
import com.zby.corelib.DeviceBean;
import com.zby.corelib.MyHexUtils;
import com.zby.corelib.utils.Crc16Util;
import com.zby.ibeacon.R;
import com.zby.ibeacon.adapter.DeviceAdapter;
import com.zby.ibeacon.database.DataBean;
import com.zby.ibeacon.database.DataDao;
import com.zby.ibeacon.utils.ExcelUtil;
import com.zby.ibeacon.utils.SharedPreApp;
import com.zby.ibeacon.utils.ToastUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.sql.DatabaseMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DeviceDataListActivity extends AppCompatActivity {

    private final String TAG = "Devicelist";

    @BindView(R.id.recyclerView)  RecyclerView       mRecyclerView;

    SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DataAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_list);
        ButterKnife.bind(this);
        initViews();
    }

    private void initViews() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new DataAdapter(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        List<DataBean> list = DataDao.queryList();
        mAdapter.setData(list);
    }

    class DataAdapter extends BGARecyclerViewAdapter<DataBean> {

        public DataAdapter(RecyclerView recyclerView) {
            super(recyclerView, R.layout.item_data);
        }

        @Override
        protected void fillData(BGAViewHolderHelper helper, int position, DataBean model) {
            helper.setText(R.id.tv_mac, model.getMac());
            helper.setText(R.id.tv_data, String.valueOf(model.getNowStep() - model.getSaveStep()))
                    .setText(R.id.tv_time, mSdf.format(new Date(model.getTime())));
            helper.setTextColorRes(R.id.tv_data, model.isSave ? R.color.text_normal : R.color.colorPrimary);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        List<DataBean> list = mAdapter.getData();
        if (list == null || list.size() ==0) {
            ToastUtils.toast("数据为空");
            return false;
        }
        String fileName = ExcelUtil.getFileName();
        String[] titles = new String[] {"mac", "运动步数", "清零记录步数", "总步数", "时间"};
        ExcelUtil.initExcel(fileName, "运动步数", titles);

        ExcelUtil.writeObjListToExcel(list, fileName, this);
        return super.onOptionsItemSelected(item);
    }

}
