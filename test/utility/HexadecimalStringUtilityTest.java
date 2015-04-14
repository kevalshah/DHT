package utility;

import org.junit.Test;

import static org.junit.Assert.*;

public class HexadecimalStringUtilityTest {

    @Test
    public void testBytesToHex() throws Exception {
        byte[] bytes = {0x0, 0x1, 0xA, 0xB};
        String expectedString = "00010A0B";
        assertEquals(expectedString, HexadecimalStringUtility.bytesToHex(bytes));
    }


    @Test
    public void testEmptyByteArray() throws Exception {
        byte[] bytes = {};
        assertEquals("", HexadecimalStringUtility.bytesToHex(bytes));
    }
}