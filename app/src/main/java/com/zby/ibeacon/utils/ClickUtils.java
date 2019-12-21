package com.zby.ibeacon.utils;


import android.support.annotation.IdRes;

/**
 * 防止按钮点击过快
 *
 * @Author: zhujiang
 * @Date: 2019/11/16 16:52
 */
public class ClickUtils {

    /**
     * 防止同一个按钮，重复点击
     */
    private static final int CLICK_TIME = 500;

    private static long sLastClickTime;
    private static int  sLastId;

    /**
     * @param viewId 点击控件
     * @return 过快点击
     */
    public static boolean isFastClick(@IdRes int viewId) {
//        if (viewId == sLastId) {
        long delay = System.currentTimeMillis() - sLastClickTime;
        if (delay < CLICK_TIME) {
            return true;
        }
//        }
        sLastId = viewId;
        sLastClickTime = System.currentTimeMillis();
        return false;
    }

}
