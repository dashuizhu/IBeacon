package com.zby.corelib;

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

    /**
     * 根据数据包长度 获得数据包最大序号
     * @param buffLength
     * @return
     */
    public static int getPackageMaxIndex(long buffLength) {
        long cout = buffLength / AppConstants.PACKAGE_DATA;
        int val = (int) (buffLength % AppConstants.PACKAGE_DATA);
        boolean hasVal = val > 0;
        if (hasVal) {
            cout += 1;
        }
        return (int) cout - 1;
    }
}
