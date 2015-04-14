package utility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class SimpleHashInvalidParamsTest {

    private String stringInput;
    private int rangeInput;

    public SimpleHashInvalidParamsTest(String stringInput, int rangeInput) {
        this.stringInput = stringInput;
        this.rangeInput = rangeInput;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> inputData() {
        return Arrays.asList(new Object[][] {
                // Parameter Order: input string, range
                {null, 5},
                {"", 10},
                {null, 0},
                {null, -1},
                {null, -2},
                {"", 0},
                {"", -1},
                {"", -2}
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSimpleHashInvalidParams() {
        HashUtility.simpleHash(stringInput, rangeInput);
    }


}
