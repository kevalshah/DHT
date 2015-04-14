package utility;

import org.junit.Test;

public class HashStringTest {

    @Test(expected = IllegalArgumentException.class)
    public void testHashStringWithNullInput() throws Exception {
        HashUtility.hashString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHashStringWithEmptyInput() throws Exception {
        HashUtility.hashString("");
    }

    @Test
    public void testHashString() throws Exception {

    }
}
