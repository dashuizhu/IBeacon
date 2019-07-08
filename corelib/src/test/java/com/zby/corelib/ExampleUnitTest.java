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


        byte[] b = "试试".getBytes();
        String str = MyHexUtils.buffer2String(b);
        System.out.println(str);

        byte[] bb = MyHexUtils.hexStringToByte(str);
        System.out.println(new String(bb));

        System.out.println(LogUtils.isLog(0));
        System.out.println(LogUtils.isLog(1));
        System.out.println(LogUtils.isLog(     2));
        System.out.println(LogUtils.isLog(3));
        System.out.println(LogUtils.isLog(4));
        System.out.println(LogUtils.isLog(5));

        int a = 11 >>3;
        int value = a & 1;
        System.out.println( a + "  " + value );

        assertEquals(4, 2 + 2);
    }

    @Test
    public void parseDataTest() {

        String s1  = "0D BB 6E 61 6D 65 E4 BD A0 E5 A5 BD 36 00 00 00 00 00 00 83";
        String s2= "12 BC 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 04 00 AA";
        String s3 = "0B BA 01 F4 00 0A 00 07 00 00 00 64 00 BB FE FF FF 0E 36 50";

        CmdProcess cmdProcess = new CmdProcess(null);

        byte[] buff = MyHexUtils.hexStringToByte(s1);
        cmdProcess.processDataCommand(null, buff, buff.length);

        buff = MyHexUtils.hexStringToByte(s2);
        cmdProcess.processDataCommand(null, buff, buff.length);

        buff = MyHexUtils.hexStringToByte(s3);
        cmdProcess.processDataCommand(null, buff, buff.length);
        //
        System.out.println(0x83+" " +( (0x0D)^0xBB^0x6e^0x61));
        ////System.out.println(0x06+" " +( (0x08)^0xA1^0x05^0x06));
        ////System.out.println(0x5+" " +( (0x04)^0xA2^0x01^0xF4));
        ////System.out.println(0x5+" " +( (0x04)^0xA2^0x00^0x00));
        ////
        //System.out.println(0xF8+" " +( (0x03)^0xB1^0x00^0x0E));
        ////System.out.println(0xB1+" " +( (0x03)^0xB2^0x00^0x00));
        //
        //
        //
        //byte[] b1= new byte[5];
        //b1[0] = (byte) 0x03;
        //b1[1] = (byte) 0xB1;
        //byte[] b2= new byte[15];
        //b2[14] = (byte) 0xB2;
        //
        //byte[] b5 = new byte[3];
        //
        //byte[] b3= new byte[2];
        //b3[0] = (byte) 0x03;
        //b3[1] = (byte) 0xB4;
        //byte[] b4= new byte[18];
        //b4[17] = (byte) 0xB7;
        //
        //
        //cmdProcess.processDataCommand(null, b1, b1.length);
        //cmdProcess.processDataCommand(null, b2, b2.length);
        //cmdProcess.processDataCommand(null, b5, b5.length);
        //cmdProcess.processDataCommand(null, b3, b3.length);
        //cmdProcess.processDataCommand(null, b4, b4.length);


    }

}