package utility;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SimpleHashTest {

    @Test
    public void testSimpleHash() {
        try {
            assertEquals(((int)'a' + (int)'b' + (int)'c') % 100, HashUtility.simpleHash("abc", 100));
        } catch(IllegalArgumentException e) {
            fail();
        }

        try {
            // ~ has the largest ASCII value found on standard keyboards
            assertEquals((32 * (int)'~') % 100, HashUtility.simpleHash("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 100));
        } catch(IllegalArgumentException e) {
            fail();
        }

    }

    @Test
    public void testLargeInputString() {

        StringBuilder testInput = new StringBuilder();

        long sum = 0;
        while(sum < Integer.MAX_VALUE) {
            testInput.append("~");
            sum += '~';
        }

        try {
            int hash = HashUtility.simpleHash(testInput.toString(), 100);
            assertEquals(hash, HashUtility.simpleHash(testInput.toString(), 100));
            assertTrue(hash >= 0 && hash < 100);
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
