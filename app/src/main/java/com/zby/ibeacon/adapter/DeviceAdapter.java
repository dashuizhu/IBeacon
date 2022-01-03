package com.zby.ibeacon.adapter;

import android.support.v7.widget.RecyclerView;
import cn.bingoogolapple.baseadapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.baseadapter.BGAViewHolderHelper;
import com.zby.corelib.DeviceBean;
import com.zby.corelib.MyHexUtils;
import com.zby.ibeacon.R;

/**
 * @author zhuj 2019/4/27 下午9:59.
 */
public class DeviceAdapter extends BGARecyclerViewAdapter<DeviceBean> {

    public DeviceAdapter(RecyclerView recyclerView) {
        super(recyclerView, R.layout.item_device);
    }

    @Override
    protected void fillData(BGAViewHolderHelper helper, int position, DeviceBean model) {
        helper.setText(R.id.tv_name, model.getName())
                .setText(R.id.tv_mac, model.getMac());

        helper.setText(R.id.tv_data, model.getDataString());
    }
}
