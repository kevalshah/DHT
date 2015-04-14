package algorithm;

public class ImmediateSuccessorRouter {

    /**
     * Determines if self is the potential IMS of the request ID
     * @param requestID - Request ID
     * @param predecessorID - Predecessor ID
     * @param selfID - Self ID
     * @return
     */
    public static boolean isSelfPotentialIMS(int requestID, int predecessorID, int selfID) {
        if(predecessorID < 0 || selfID < 0) {
            throw new IllegalArgumentException();
        }

        return isInRange(requestID, predecessorID, selfID);
    }


    /**
     * Finds the potential IMS of a request ID
     * @param requestID - Request ID
     * @param predecessorID - Predecessor ID
     * @param selfID - Self ID
     * @param successorList - successor list IDs
     * @return - ID of potential IMS of request ID
     * @throws NoPotentialIMSException - if no potential IMS found
     */
    public static int findPotentialIMS(int requestID, int predecessorID, int selfID, int[] successorList)
            throws NoPotentialIMSException {
        if(requestID < 0 || selfID < 0 || successorList == null || successorList.length == 0) {
            throw new IllegalArgumentException();
        }

        if(predecessorID > 0) {
            // Check predecessor and self
            if(isInRange(requestID, predecessorID, selfID)) {
                return selfID;
            }
        }

        // Check successor list
        for(int i = 0; i < successorList.length; i++) {
            if(i == 0) {
                if(isInRange(requestID, selfID, successorList[i])) {
                    return successorList[i];
                }
            } else {
                if(isInRange(requestID, successorList[i-1], successorList[i])) {
                    return successorList[i];
                }
            }
        }

        // Throw exception cause no potential IMS detected
        throw new NoPotentialIMSException();
    }


    /**
     * Checks to see if request ID falls within the range of first ID and second ID
     * @param requestID - Request ID to check if in range
     * @param firstID - First ID in range
     * @param secondID - Second ID in range
     * @return
     */
    protected static boolean isInRange(int requestID, int firstID, int secondID) {
        if(requestID < 0 || firstID < 0 || secondID < 0) {
            throw new IllegalArgumentException();
        }

        if((firstID < requestID && requestID <= secondID) ||
                (requestID <= secondID && secondID < firstID) ||
                (secondID < firstID && firstID <= requestID)) {
            return true;
        }

        return false;
    }




}
