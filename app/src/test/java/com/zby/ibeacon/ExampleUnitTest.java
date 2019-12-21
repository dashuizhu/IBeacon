package com.zby.ibeacon;

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
        String str= "MD5:  B5:B7:14:73:71:20:68:DA:7B:19:DE:AF:EF:FC:EC:AF";
        str = str.replace(":","").toLowerCase();

        String str2 = "DE:DE:76:F7:FF:F1:B4:0D:C8:EA:12:FE:75:44:D6:F4:EC:30:0D:6A";
        str2 = str2.replace(":","").toLowerCase();
        System.out.println(str);
        System.out.println(str2);
    }
}