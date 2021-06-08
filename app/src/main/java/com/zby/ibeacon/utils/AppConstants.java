package com.zby.ibeacon.utils;

/**
 * @author zhuj 2021/6/7 10:32 PM.
 */
public class AppConstants {

    public static int PACKAGE_DATA = 16;

    public static boolean isDemo = false;

    public static boolean isEncrypt = true;

    public static void setEncrypt(boolean encrypt) {
        isEncrypt = encrypt;
        PACKAGE_DATA = isEncrypt ? 16 : 20;
    }
}
