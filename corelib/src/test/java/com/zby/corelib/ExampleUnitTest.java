package com.zby.corelib;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        float ff = Math.round(740 * (256 * 7 + 128) / 2047) / 100.0f;

        byte[] b = "试试".getBytes();
        String str = MyHexUtils.buffer2String(b);
        System.out.println(str + " " + ff);

        byte[] bb = MyHexUtils.hexStringToByte(str);
        System.out.println(new String(bb));

        System.out.println(LogUtils.isLog(0));
        System.out.println(LogUtils.isLog(1));
        System.out.println(LogUtils.isLog(     2));
        System.out.println(LogUtils.isLog(3));
        System.out.println(LogUtils.isLog(4));
        System.out.println(LogUtils.isLog(5));

        byte b2 = (byte)0x81;

        int a = 31 >> 4;
        int value = a & 1;
        System.out.println( a + "  " + value + b2 );

        assertEquals(4, 2 + 2);
    }

    @Test
    public void parseDataTest() {

        String s1  = "0D FF 05 B1 6D EF E4 BD A0 E5 A5 BD 36 00 00 00 00 00 00 83";
        String s2= "12 FF 08 B5 03 01 01 ";
        String s3 = "01 EF 00 AA F4 00 0A 00 07 00 00 00 64 00 BB FE FF FF 0E 36 50";

        CmdProcess cmdProcess = new CmdProcess(null);

        byte[] buff = MyHexUtils.hexStringToByte(s1);
        cmdProcess.processDataCommand(null, buff, buff.length);

        buff = MyHexUtils.hexStringToByte(s2);
        cmdProcess.processDataCommand(null, buff, buff.length);

        buff = MyHexUtils.hexStringToByte(s3);
        cmdProcess.processDataCommand(null, buff, buff.length);

    }

    @Test
    public void sendTest() {



        String key = "1111111111111111";

        byte[] b1 = CmdEncrypt.sendMessage(CmdPackage.setKey("1234567890123456"));
        //System.out.println(MyHexUtils.buffer2String(b1));

        //b1 = CmdEncrypt.sendMessage(CmdPackage.setOpen("010203040506", 0));
        String s1 = MyHexUtils.buffer2String(b1);
        System.out.println(s1);

        b1  = AESCBCUtil.encrypt(b1, key);
        System.out.println(MyHexUtils.buffer2String(b1));

        byte[] deB = AESCBCUtil.decrypt(b1, key);
        System.out.println("解密 "+MyHexUtils.buffer2String(deB));


        String deStr = "38 AF 98 01 60 EF 9E 27 7B D0 A7 43 38 92 A9 6E ";
        b1 = AESCBCUtil.decrypt(MyHexUtils.hexStringToByte(deStr), key);
        System.out.println("解密 "+MyHexUtils.buffer2String(b1));

        deStr = "94 00 6E 20 2F A6 3B A8 F0 B5 A8 9E 6D 4C 2F A3 ";
        b1 = AESCBCUtil.decrypt(MyHexUtils.hexStringToByte(deStr), key);
        System.out.println("解密 "+MyHexUtils.buffer2String(b1));


    }

    @Test
    public void add16() {
        String hello="生成秘钥生。";
        int len=hello.getBytes().length;
        if(len%8!=0){
                System.out.println("不是8的整数倍");
                byte[] hellotemp=new byte[len+(8-len%8)];
                for(int i=0;i<len;i++){
                    hellotemp[i]=hello.getBytes()[i];
                }
                hello=new String(hellotemp);
            }
        System.out.println(hello.getBytes().length);
    }

}