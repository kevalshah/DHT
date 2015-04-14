package algorithm;

public class KeyForwarder {

    /**
     * Algorithm that returns the node ID that is potentially responsible for the keyID
     * Works as follows: <br>
     * 1) If key ID == predecessor ID -> Returns predecessor ID <br>
     * 2) Else if key ID == self ID OR <br>
     *            predecessor ID < key ID < self ID OR <br>
     *            key ID < self ID < predecessor ID OR <br>
     *            self ID < predecessor ID < key ID -> Returns self ID <br>
     * 3) Otherwise find smallest node ID >= key ID, if none found then return node with minimum ID if
     *    its index in the successor list != 0. Else return the node with max ID.
     * @param keyID - Input key ID
     * @param selfID - This node's ID
     * @param predecessorID - This node's predecessor ID
     * @param successorList - This node's successor list ID(s) in order of successors
     * @return - the node ID that is potentially responsible for the keyID
     * @throws IllegalArgumentException if key ID < 0 OR self ID < 0
     * @throws IllegalStateException if predecessor ID >= 0 AND successor list is empty
     */
    public static int routeKeyToNode(int keyID, int selfID, int predecessorID, int[] successorList) {

        // If invalid arguments, throw exception
        if(keyID < 0 || selfID < 0) {
            throw new IllegalArgumentException();
        }

        // Check if in illegal state
        if(predecessorID >= 0 && successorList.length == 0) {
            throw new IllegalStateException();
        }

        // Checks that involve a valid predecessor ID (i.e. >= 0)
        if(predecessorID >= 0) {
            if(keyID == predecessorID) {
                return predecessorID;
            } else if((predecessorID < keyID && keyID < selfID) ||
                    (keyID < selfID && selfID < predecessorID) ||
                    (selfID < predecessorID && predecessorID < keyID)) {
                return selfID;
            }
        }

        // Check if key ID matches self ID or if the successor list is empty
        if(keyID == selfID || (predecessorID < 0 && successorList.length == 0)) {
            return selfID;
        }

        int minNodeID = successorList[0];
        int minNodeIDGreaterThanKeyID = -1;
        int minNodeIDIndex = 0;
        int maxNodeID = successorList[0];

        for(int i = 0; i < successorList.length; i++) {
            int successorID = successorList[i];

            // Keep track of node with minimum node ID and the index in list
            if(successorID < minNodeID) {
                minNodeID = successorID;
                minNodeIDIndex = i;
            }

            // Keep track of node with maximum node ID
            if(successorID > maxNodeID) {
                maxNodeID = successorID;
            }

            // Keep track of smallest node ID >= key ID
            if(successorID >= keyID) {
                if(minNodeIDGreaterThanKeyID < 0 || successorID < minNodeIDGreaterThanKeyID) {
                    minNodeIDGreaterThanKeyID = successorID;
                }
            }
        }

        if(minNodeIDGreaterThanKeyID >= 0) {
            return minNodeIDGreaterThanKeyID;
        }

        if(minNodeIDIndex != 0) {
            return minNodeID;
        }

        return maxNodeID;
    }



}
