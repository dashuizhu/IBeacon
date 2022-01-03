package com.zby.corelib;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuj 2019/4/29 下午3:39.
 */
 public class LogUtils {
    static final String pre = "ble_";
    static final int logLevel = 63;

    public static boolean isLog(int level) {
        return ((logLevel >>level) & 1) == 1;
    }

    static void logV(String tag, String message) {
        if (isLog(0)) {
            Log.v(pre+tag, message);
            writeLogToFile(tag, message);
        }
    }

    static void logD(String tag, String message) {
        if (isLog(1)) {
            Log.d(pre+tag, message);
            writeLogToFile(tag, message);
        }
    }

    static void logI(String tag, String message) {
        if (isLog(2)) {
            Log.i(pre+tag, message);
            writeLogToFile(tag, message);
        }
    }

    static void logW(String tag, String message) {
        if (isLog(3)) {
            Log.w(pre+tag, message);
            writeLogToFile(tag, message);
        }
    }

    static void logE(String tag, String message) {
        if (isLog(4)) {
            Log.e(pre+tag, message);
            writeLogToFile(tag, message);
        }
    }

    static void logSout(String message) {
        if (isLog(5)) {
            System.out.println(pre+message);
            writeLogToFile("system", message);
        }
    }



    private static FileOutputStream fos       = null;
    private static String           mFileName = null;

    private static SimpleDateFormat formatter        = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat timelogFormatter = new SimpleDateFormat("HH:mm:ss");

    private static final String PARENT_DIR = "BleTest";
    private static final String LOG_DIR = "CmdLog";

    private final static String Prefix = "TSocket_";

    public static ThreadPoolExecutor mExecutor;

    public static LinkedBlockingQueue<Runnable> mRunnables;

    public static void writeLogToFile(final String tag,final String msg)
    {
        if(msg == null || msg.equals(""))
        {
            Log.v(tag, "writeLogToFile failed1");
            return;
        }
        if (mExecutor == null) {
            mRunnables = new LinkedBlockingQueue<>(200);
            mExecutor = new ThreadPoolExecutor(32, 64, 20, TimeUnit.SECONDS, mRunnables);
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(fos != null) {
                        Log.e(tag, msg);
                        File tmpFile = new File(mFileName);
                        if (!tmpFile.exists()) {
                            closeLogFile();
                            openLogFile();
                        }
                        String time = timelogFormatter.format(new Date())+"   ";
                        StringBuffer sb = new StringBuffer();
                        sb.append(time);
                        sb.append(tag);
                        int len = tag.length();
                        int timelen = time.length();
                        sb.setLength(timelen+32);
                        for(int i=0;i< 32 - len;i++)
                        {
                            sb.setCharAt(timelen+len+i, ' ');
                        }
                        sb.append(msg);
                        sb.append("\r\n");
                        fos.write(sb.toString().getBytes());

                    }
                    else
                    {
                        openLogFile();
                        writeLogToFile(tag, msg);
                    }
                } catch (Exception e) {
                    Log.v(tag, "writeLogToFile err111");
                    e.printStackTrace();
                }
            }
        });
    }

    private static void openLogFile()
    {
        try {
            //long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "log_" + time + ".txt";
            File parentFile = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = Environment.getExternalStorageDirectory();
                parentFile = new File(file, PARENT_DIR+"/"+LOG_DIR);
                if(!parentFile.exists())
                {
                    parentFile.mkdirs();
                }
                if(parentFile != null)
                {
                    mFileName = parentFile.getAbsolutePath() +"/"+ fileName;
                    Log.e(Prefix, "写入文件" + mFileName);
                    fos = new FileOutputStream(mFileName,true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void closeLogFile()
    {
        try {
            if(fos != null)
            {
                fos.flush();
                fos.close();
                fos = null;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
