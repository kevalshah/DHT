package algorithm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;


@RunWith(Parameterized.class)
public class KeyForwardingOnSingleNodeTest {

    private int[] successorList;
    private int keyID;
    private int selfID;
    private int predecessorID;
    private int expectedID;
    private boolean exceptionExpected;

    public KeyForwardingOnSingleNodeTest(int predecessorID, int selfID, int[] successorList, int keyID, int expectedID,
                                         boolean exceptionExpected) {
        this.successorList = successorList;
        this.keyID = keyID;
        this.selfID = selfID;
        this.predecessorID = predecessorID;
        this.expectedID = expectedID;
        this.exceptionExpected = exceptionExpected;
    }


    @Test
    public void testRouteKeyToNode() throws Exception {

        if(exceptionExpected) {
            try {
                KeyForwarder.routeKeyToNode(keyID, selfID, predecessorID, successorList);
                fail();
            } catch(IllegalArgumentException e) {

            } catch(IllegalStateException e) {

            }
        } else {
            try {
                int actualID = KeyForwarder.routeKeyToNode(keyID, selfID, predecessorID, successorList);
                assertEquals(expectedID, actualID);
            } catch(Exception e) {
                fail();
            }
        }

    }



    @Parameterized.Parameters
    public static Collection<Object[]> inputData() {
        return Arrays.asList(new Object[][]{
                // Parameter Order: predecessor ID, self ID, successorList[], key ID, expected ID, exception expected

                // 1) Cases where self or predecessor is chosen
                {250, 253, new int[]{5, 7, 10, 20}, 253, 253, false}, // Self ID == Key ID -> expected Self ID
                {250, 253, new int[]{5, 7, 10, 20}, 250, 250, false}, // Predecessor ID == Key ID -> expected Predecessor ID
                {250, 252, new int[]{5, 7, 10, 20}, 251, 252, false}, // Predecessor ID < Key ID < Self ID -> expected Self ID
                {1, 3, new int[]{5, 7, 10, 20}, 2, 3, false}, // Predecessor ID < Key ID < Self ID -> expected Self ID
                {250, 2, new int[]{5, 7, 10, 20}, 0, 2, false}, // Key ID < Self ID < Predecessor ID -> expected Self ID
                {25, 3, new int[]{5, 7, 10, 20}, 255, 3, false}, // Self ID < Predecessor ID < Key ID -> expected Self ID

                // 2) Cases where we need to search the successor list
                // Algorithm: If a node ID == key ID then return that node. Otherwise find smallest node ID >= key ID, if none found then return node with minimum ID if
                // its index in the successor list != 0. Else return the node with max ID.

                // 2.1) Node in the successor list == key ID
                {25, 3, new int[]{6, 7, 11, 19}, 6, 6, false},
                {25, 3, new int[]{6, 7, 11, 19}, 7, 7, false},
                {25, 3, new int[]{6, 7, 11, 19}, 11, 11, false},
                {25, 3, new int[]{6, 7, 11, 19}, 19, 19, false},
                {1, 3, new int[]{5, 7, 10, 0}, 0, 0, false},
                {3, 4, new int[]{5, 7, 1, 2}, 1, 1, false},

                // 2.2) If no node ID == key ID in list, then return smallest node ID > key ID in list
                {25, 3, new int[]{5, 7, 10, 20}, 4, 5, false}, // Return smallest node ID > key ID
                {25, 3, new int[]{5, 7, 10, 20}, 6, 7, false}, // Return smallest node ID > key ID
                {25, 3, new int[]{5, 7, 10, 20}, 9, 10, false}, // Return smallest node ID > key ID
                {25, 3, new int[]{5, 7, 10, 20}, 15, 20, false}, // Return smallest node ID > key ID
                {1, 2, new int[]{5, 7, 10, 20}, 3, 5, false}, // Return smallest node ID > key ID
                {4, 5, new int[]{7, 10, 0, 3}, 1, 3, false}, // Return smallest node ID > key ID

                // 2.3) If there is no node ID > key ID in list AND the index of node with min ID != 0, then return smallest node ID
                {1, 3, new int[]{5, 7, 10, 0}, 11, 0, false}, // Return smallest node ID
                {1, 3, new int[]{5, 7, 10, 0}, 255, 0, false}, // Return smallest node ID
                {4, 6, new int[]{9, 11, 0, 2}, 12, 0, false}, // Return smallest node ID
                {4, 6, new int[]{9, 0, 1, 2}, 12, 0, false}, // Return smallest node ID

                // 2.4) If there is no node ID > key ID in list AND the index of node with min ID == 0, then return largest node ID
                {1, 3, new int[]{5, 7, 10, 12}, 13, 12, false}, // Return largest node ID
                {4, 6, new int[]{9, 11, 14, 235}, 250, 235, false}, // Return largest node ID
                {4, 6, new int[]{9, 23, 25, 27}, 29, 27, false}, // Return largest node ID

                // 3) Cases with illegal arguments (key ID < 0 OR self ID < 0) -> Exception expected

                // 3.1) Self ID < 0
                {1, -1, new int[]{5, 7, 10, 12}, 13, 12, true},

                // 3.2) Key ID < 0
                {1, 2, new int[]{5, 7, 10, 12}, -1, 12, true},

                // 3.3) Self ID < 0 AND Key ID < 0
                {1, -1, new int[]{5, 7, 10, 12}, -1, 12, true},

                // 4) Cases with predecessor ID < 0
                {-1, 0, new int[]{5, 7, 10, 12}, 0, 0, false}, // Self ID == Key ID -> expected Self ID
                {-1, 0, new int[]{5, 7, 10, 12}, 2, 5, false}, // Smallest node ID >= key ID in successor list
                {-1, 0, new int[]{5, 7, 10, 12}, 6, 7, false}, // Smallest node ID >= key ID in successor list
                {-1, 0, new int[]{5, 7, 10, 12}, 10, 10, false}, // Smallest node ID >= key ID in successor list
                {-1, 0, new int[]{5, 7, 10, 12}, 11, 12, false}, // Smallest node ID >= key ID in successor list
                {-1, 20, new int[]{30, 35, 40, 0}, 13, 30, false}, // Min ID with index != 0 in successor list

                // 5) Cases with empty successor list
                {0, 20, new int[]{}, 13, 20, true}, // Predecessor ID >= 0 -> exception expected
                {-1, 20, new int[]{}, 25, 20, false}, // Self ID

        });
    }





}