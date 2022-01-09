package com.zby.ibeacon;

import com.zby.corelib.MyHexUtils;
import com.zby.corelib.utils.Crc16Util;
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
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() {
        System.out.println(String.format("%05d",1234567));
        String str = "0D FF DA 13 03 01 01 DA 06 01 00 00 4B 2C";
        byte[] buff = MyHexUtils.hexStringToByte(str);
        byte[] data = Crc16Util.getCrc16(buff);


        byte[] buff1 = new byte[4];
        System.arraycopy(buff, 8, buff1, 0, 4);
        //byte[] buff2 = new byte[2];
        //System.arraycopy(buff, 12, buff2, 0, 2);
        String s1 = MyHexUtils.buffer2String(buff1);
        //String s2 = MyHexUtils.buffer2String(buff2);

        System.out.println(s1 );
        //System.out.println(s2);
        System.out.println(MyHexUtils.buffer2String(data));



        byte[] buffbb = new byte[12];
        System.arraycopy(buff, 0, buffbb, 0, buffbb.length);
        byte[] crcData = Crc16Util.getCrc16(buffbb);
        System.out.println("-----"+MyHexUtils.buffer2String(crcData));
        if (crcData[0] == buff[12] && crcData[1] == buff[13]) {
            //if (arg2[13] == 28) {
            System.out.println("test ----222");
        } else {
            System.out.println("testfalse");
        }


        byte[] buffData = new byte[4];
        System.arraycopy(buff, 8, buffData, 0, 4);
        int value = 0;
        for (int i=3; i>=0; i--) {
            value += buffData[i] * Math.pow(256, i);
        }
        System.out.println("value -- "+value);

    }
}