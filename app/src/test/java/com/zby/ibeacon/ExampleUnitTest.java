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
        String str = "0D FF DA 13 03 01 01 DA AF 00 00 00";
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

    }
}