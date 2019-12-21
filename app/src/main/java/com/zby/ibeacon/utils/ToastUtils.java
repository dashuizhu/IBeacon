package com.zby.ibeacon.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.StringRes;
import android.widget.MediaController;
import android.widget.Toast;

/**
 * toast 工具
 * @Author: zhujiang
 * @Date: 2019/10/29 15:25
 */
public class ToastUtils extends Toast {
    private volatile static Toast mToast;
    private static Context mContext;

    private ToastUtils(Context context) {
        super(context);
    }

    private static Toast getToast(Context context) {
        if (mToast == null) {
            synchronized (ToastUtils.class) {
                mContext = context.getApplicationContext();
                mToast = Toast.makeText(mContext, "", Toast.LENGTH_LONG);
            }
        }
        return mToast;
    }

    public static void toast(Context context, @StringRes int message) {
        toast(context, context.getString(message));
    }

    public static void toast(Context context, String message) {
        toastRes(context, message);
    }

    private static void toastRes(Context context, String str) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //9.0上系统机制修改，重复toast 会自动调用隐藏上一个， 不能共用一个toast
            Toast.makeText(mContext, str, Toast.LENGTH_LONG).show();
        } else {
            Toast toast = getToast(context);
            toast.setText(str);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        }
    }

}