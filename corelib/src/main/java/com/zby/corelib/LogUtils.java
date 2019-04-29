package com.zby.corelib;

import android.util.Log;

/**
 * @author zhuj 2019/4/29 下午3:39.
 */
 class LogUtils {
    static final String pre = "ble_";
    static final int logLevel = 31;

    public static boolean isLog(int level) {
        return ((logLevel >>level) & 1) == 1;
    }

    static void logV(String tag, String message) {
        if (isLog(0)) {
            Log.v(pre+tag, message);
        }
    }

    static void logD(String tag, String message) {
        if (isLog(1)) {
            Log.d(pre+tag, message);
        }
    }

    static void logI(String tag, String message) {
        if (isLog(2)) {
            Log.i(pre+tag, message);
        }
    }

    static void logW(String tag, String message) {
        if (isLog(3)) {
            Log.w(pre+tag, message);
        }
    }

    static void logE(String tag, String message) {
        if (isLog(4)) {
            Log.e(pre+tag, message);
        }
    }

    static void logSout(String message) {
        if (isLog(5)) {
            System.out.println(pre+message);
        }
    }

}
