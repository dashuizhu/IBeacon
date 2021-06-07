package com.zby.ibeacon.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.StringRes;
import android.widget.Toast;
import com.zby.ibeacon.AppApplication;

/**
 * toast 工具
 * @Author: zhujiang
 * @Date: 2019/10/29 15:25
 */
public class ToastUtils extends Toast {
    private volatile static Toast mToast;

    private ToastUtils(Context context) {
        super(context);
    }

    private static Toast getToast() {
        if (mToast == null) {
            synchronized (ToastUtils.class) {
                mToast = Toast.makeText(AppApplication.getContext(), "", Toast.LENGTH_LONG);
            }
        }
        return mToast;
    }

    public static void toast(@StringRes int message) {
        toast(AppApplication.getContext().getString(message));
    }

    public static void toast(String message) {
        toastRes(message);
    }

    private static void toastRes(String str) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //9.0上系统机制修改，重复toast 会自动调用隐藏上一个， 不能共用一个toast
            if (mToast != null) {
                mToast.cancel();
            }
            //小米手机上， 要先设置为null 再setText， 不然会自动添加 应用名:str
            mToast = Toast.makeText(AppApplication.getContext(), "", Toast.LENGTH_LONG);
            mToast.setText(str);
            mToast.show();
        } else {
            Toast toast = getToast();
            toast.setText(str);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        }
    }

}