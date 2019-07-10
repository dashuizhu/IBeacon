package com.zby.corelib;

import android.text.TextUtils;
import java.util.HashSet;
import java.util.Set;

/**
 * 对于读取的数据，是分批发送上来， 这里要做截取 拼凑成完整协议，进行解析
 * 对收到的数据字节，全部缓存起来， 等识别出一条符合协议的数据时，将截取出来的协议数据进行解析
 * Created by zhuj on 2016/7/13 16:19.
 */
class CmdProcess {

    private final static String TAG = "cmdProcess";

    private final static int CMD_HEAD = (byte) 0xFF;
    private final static int CMD_END  = (byte) 0xEF;

    private ICmdParseInterface mCmdParse;

    private CmdProcess() {
    }

    CmdProcess(ICmdParseInterface iCmdParseInterface) {
        this.mCmdParse = iCmdParseInterface;
    }

    /**
     * 在构成一条协议数据时 清空
     */
    private int    data_length;
    /**
     * 当前缓存的数据长度， 收到的字节个数， 在构成一条协议数据时 清0
     */
    private byte[] data_command = new byte[512];

    //private Set<Byte> mCmdSet = new HashSet() {
    //    {
    //        add(CmdParseImpl.TYPE_CHANGEMODE);
    //        add(CmdParseImpl.TYPE_CHECKID);
    //        add(CmdParseImpl.TYPE_OPEN);
    //        add(CmdParseImpl.TYPE_SETKEY);
    //        add(CmdParseImpl.TYPE_STATUS);
    //    }
    //};

    /**
     * 将收到的数据 截取符合协议的子集
     */
    synchronized void processDataCommand(DeviceBean db, byte[] command, int length) {
        if (command == null || length == 0) {
            return;
        }
        //吸入缓存区
        System.arraycopy(command, 0, data_command, data_length, length);
        data_length += length;
        processData(db);
    }

    /**
     * 从缓存区，遍历， 识别到 数据码时， 截取子数据，判断是否是协议，
     * 是协议就进行解析， 并且，将剩下数据 重置到缓存区。
     */
    private void processData(DeviceBean db) {
        if (data_length < 4) {
            //最短协议都有5个字节
            return;
        }

        //AES加密模式
        String key = db.getKey();
        if (!TextUtils.isEmpty(key)) {
            //凑够16个字节，进行解密
            if (data_length % 16 == 0) {
                byte[] buff = new byte[data_length];
                System.arraycopy(data_command, 0, buff, 0, buff.length);

                byte[] decrBuff = AESCBCUtil.decrypt(buff, key);
                int endIndex = 0;
                for (int i = decrBuff.length - 1; i >= 0; i--) {
                    if (decrBuff[i] != 0) {
                        endIndex = i;
                        break;
                    }
                }
                byte[] checkBuff = new byte[endIndex];
                System.arraycopy(decrBuff, 0, checkBuff, 0, checkBuff.length);
                byte[] encryBuff = CmdEncrypt.processMessage(checkBuff);
                if (encryBuff != null) {
                    //解析成功，
                    parseData(db, encryBuff);
                    cleanCache();
                    return;
                }
            }
        }

        //非加密模式
        for (int index = 0; index < data_length; index++) {

            //判断是否含有指定的返回识别码
            if (data_command[index] == CMD_HEAD) {
                if (index + 1 >= data_length) {
                    break;
                }
                int length = data_command[index + 1];
                //是否是 包尾
                if (data_command[index + length - 1] == CMD_END) {

                    byte[] checkBuff = new byte[length];
                    System.arraycopy(data_command, index, checkBuff, 0, checkBuff.length);
                    //检测完毕的数据
                    byte[] encryBuff = CmdEncrypt.processMessage(checkBuff);
                    if (encryBuff != null) {
                        //解析成功，
                        parseData(db, encryBuff);
                        //把剩下数据提取出来 ，重新组成缓存区
                        int dataIndex = index + length;
                        if (dataIndex < data_length) {
                            byte[] othenBuff = new byte[data_length - dataIndex];
                            System.arraycopy(data_command, dataIndex, othenBuff, 0,
                                    othenBuff.length);
                            data_command = new byte[512];
                            data_length = 0;
                            processDataCommand(db, othenBuff, othenBuff.length);
                            return;
                        } else { //正好解析完，缓存区所有数据
                            cleanCache();
                            return;
                        }
                    }
                }
            }
        }
    }

    protected void cleanCache() {
        data_command = new byte[512];
        data_length = 0;
    }

    private void parseData(DeviceBean db, byte[] buffer) {
        //System.out.println("解析数据 ：" + MyHexUtils.buffer2String(buffer));
        if (mCmdParse != null) {
            mCmdParse.parseData(db, buffer);
        }
    }
}
