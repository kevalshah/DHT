package algorithm;

public class ImmediateSuccessorFinder {


    /**
     * Algorithm that finds the potential immediate successor of a joining node <br>
     * Works as follows: <br>
     * 1) If predecessor ID < joining node ID < self ID OR <br>
     *            joining node ID < self ID < predecessor ID OR <br>
     *            self ID < predecessor ID < joining node ID -> Returns self ID <br>
     * 3) Otherwise find smallest node ID > joining node ID, if none found then return node with minimum ID if
     *    its index in the successor list != 0. Else return the node with max ID.
     * @param joiningNodeID - ID of the joining node
     * @param selfID - This node's ID
     * @param predecessorID - This node's predecessor ID
     * @param successorList - This node's successor list
     * @return - the potential immediate successor ID of the joining node
     * @throws IllegalArgumentException if joining node ID < 0 OR self ID < 0
     * @throws IllegalStateException if predecessor ID >= 0 AND successor list is empty
     */
    public static int findPotentialImmediateSuccessor(int joiningNodeID, int selfID, int predecessorID, int[] successorList) {

        // If invalid arguments, throw exception
        if(joiningNodeID < 0 || selfID < 0) {
            throw new IllegalArgumentException();
        }

        // If no predecessor and no successors, return self
        if(predecessorID < 0 && successorList.length == 0) {
            return selfID;
        }

        // Check if in illegal state
        if(predecessorID >= 0 && successorList.length == 0) {
            throw new IllegalStateException();
        }

        // Checks that involve a valid predecessor ID (i.e. >= 0)
        if(predecessorID >= 0) {
            if((predecessorID < joiningNodeID && joiningNodeID < selfID) ||
                    (joiningNodeID < selfID && selfID < predecessorID) ||
                    (selfID < predecessorID && predecessorID < joiningNodeID)) {
                return selfID;
            }
        }

        int minNodeID = successorList[0];
        int minNodeIDGreaterThanJoiningNodeID = -1;
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

            // Keep track of smallest node ID > joining node ID
            if(successorID > joiningNodeID) {
                if(minNodeIDGreaterThanJoiningNodeID < 0 || successorID < minNodeIDGreaterThanJoiningNodeID) {
                    minNodeIDGreaterThanJoiningNodeID = successorID;
                }
            }

            if(joiningNodeID == successorID) {
                throw new IllegalStateException();
            }
        }

        if(minNodeIDGreaterThanJoiningNodeID >= 0) {
            return minNodeIDGreaterThanJoiningNodeID;
        }

        if(minNodeIDIndex != 0) {
            return minNodeID;
        }

        return maxNodeID;
    }








}
