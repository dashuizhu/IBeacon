package com.zby.ibeacon.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.zby.ibeacon.AppApplication;
import java.util.Calendar;

/**
 * 用来保存app的 数据，更换用户时可以不清空
 *
 * @author zhuj
 * @date 2017/6/15 下午6:11
 */
public class SharedPreApp extends BaseSharedPre {

    private static volatile SharedPreApp mSharedPre;

    /**
     * 保存在手机里面的文件名
     */
    public static final String FILE_NAME = "share_app_data";

    public static final String KEY_MAC = "mymac";
    public static final String KEY_NUM = "number";
    public static final String KEY_NUM_TIME = "time";

    @Override
    SharedPreferences getSharedPreferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreApp getInstance() {
        if (mSharedPre == null) {
            synchronized (SharedPreApp.class) {
                if (mSharedPre == null) {
                    mSharedPre = new SharedPreApp();
                }
            }
        }
        return mSharedPre;
    }

    public String getBindMac() {
        return (String) get(AppApplication.getContext(), KEY_MAC, "");
    }

    public void setBindMac(String mac) {
        put(AppApplication.getContext(), KEY_MAC, mac);
    }

    /**
     * 返回今天的步数
     * @return
     */
    public int getKeyNum() {
        long time = (long) get(AppApplication.getContext(), KEY_NUM_TIME, 0L);
        if (!isToday(time)) {
            return 0;
        }
        return (int) get(AppApplication.getContext(), KEY_NUM, 0);
    }

    public void setKeyNum(int num) {
        put(AppApplication.getContext(), KEY_NUM, num);
        put(AppApplication.getContext(), KEY_NUM_TIME, System.currentTimeMillis());
    }

    private boolean isToday(long time) {
        Calendar today = Calendar.getInstance();
        int yearToday = today.get(Calendar.YEAR);
        int dayToday = today.get(Calendar.DAY_OF_YEAR);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTimeInMillis(time);
        return (yearToday == timeCal.get(Calendar.YEAR) && dayToday == timeCal.get(Calendar.DAY_OF_YEAR));
    }

}
