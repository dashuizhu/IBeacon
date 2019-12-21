package com.zby.corelib;

import java.util.HashSet;
import java.util.Set;

/**
 * 对于读取的数据，是分批发送上来， 这里要做截取 拼凑成完整协议，进行解析
 * 对收到的数据字节，全部缓存起来， 等识别出一条符合协议的数据时，将截取出来的协议数据进行解析
 * Created by zhuj on 2016/7/13 16:19.
 */
@Deprecated
class CmdProcess {

    private final static String TAG = "cmdProcess";

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

    private Set<Byte> mCmdSet = new HashSet() {
        {
//            add(CmdParseImpl.type_password);
//            add(CmdParseImpl.type_frequency);
//            add(CmdParseImpl.type_major);
//            add(CmdParseImpl.type_minor);
//            add(CmdParseImpl.type_uuid_set);
//            add(CmdParseImpl.type_password_set);
//            add(CmdParseImpl.type_name);
//            add(CmdParseImpl.type_rate);
//            add(CmdParseImpl.type_light);
//            add(CmdParseImpl.type_status);
//            add(CmdParseImpl.type_name_read);
//            add(CmdParseImpl.type_uuid_red);
//            add(CmdParseImpl.type_cmd);
        }
    };

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
        for (int index = 0; index < data_length; index++) {
            //判断是否含有指定的返回识别码
            if (mCmdSet.contains(data_command[index])) {
                if (index > 0) {
                    //长度判断
                    //数据长度写在了  识别码前面，所以下标-1，
                    //int checkBuffLenght = MyByteUtils.byteToInt(data_command[index - 1]);
                    int checkBuffLenght = 20;
                    //剩余的数据，长度超过一条协议， 每一条协议都是20个字节
                    if (index + 19 <= data_length) {
                        //需要检测的数据组， +1是长度字节本身自己。
                        byte[] checkBuff = new byte[checkBuffLenght];
                        System.arraycopy(data_command, index - 1, checkBuff, 0, checkBuff.length);
                        //检测完毕的数据
                        byte[] encryBuff = CmdEncrypt.processMessage(checkBuff);
                        if (encryBuff != null) {
                            //解析成功，
                            parseData(db, encryBuff);
                            //把剩下数据提取出来 ，重新组成缓存区
                            int dataIndex = index + checkBuffLenght;
                            if (dataIndex < data_length) {
                                byte[] othenBuff = new byte[data_length - dataIndex];
                                System.arraycopy(data_command, dataIndex, othenBuff, 0,
                                        othenBuff.length);
                                data_command = new byte[512];
                                data_length = 0;
                                processDataCommand(db, othenBuff, othenBuff.length);
                                return;
                            } else { //正好解析完，缓存区所有数据
                                data_command = new byte[512];
                                data_length = 0;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void parseData(DeviceBean db, byte[] buffer) {
        //System.out.println("解析数据 ：" + MyHexUtils.buffer2String(buffer));
        if (mCmdParse != null) {
            mCmdParse.parseData(db, buffer);
        }
    }
}
