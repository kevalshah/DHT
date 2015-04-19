package nodelist;

import java.util.ArrayList;

public class RemovedNodeList {

    private ArrayList<TimestampNode> removedNodes;
    private int maxCapacity = 20;

    private static RemovedNodeList removedNodeList;

    protected RemovedNodeList(int capacity) {
        removedNodes = new ArrayList<TimestampNode>();
        maxCapacity = capacity;
    }

    public static RemovedNodeList getInstance() {
        if(removedNodeList == null) {
            removedNodeList = new RemovedNodeList(20);
        }

        return removedNodeList;
    }


    /**
     * Adds a node to the removed nodes list
     * @param node - Node to add to removed nodes list
     */
    public void addRemovedNode(Node node) {
        if(removedNodes.size() >= maxCapacity) {
            removedNodes.remove(0);
        } else {
            removedNodes.add(new TimestampNode(node));
        }
    }


    public TimestampNode getRemovedNode(Node nodeToGet) {
        for(TimestampNode node : removedNodes) {
            if(nodeToGet.equals(node)) {
                return node;
            }
        }

        return null;
    }



    /**
     * Checks whether input node is in removed nodes list
     * @param nodeToCheck - Node to check
     * @return
     */
    public boolean isNodeInRemovedList(Node nodeToCheck) {
        if(removedNodes.size() > 0) {
            for(TimestampNode node : removedNodes) {
                if(nodeToCheck.equals(node)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Checks whether the input node's timestamp has expired. Expects input node to be in removed list.
     * @param nodeToCheck - Node to check
     * @return - True if timestamp has expired for the input node. False, if timestamp has not expired OR node is not in list.
     */
    public boolean isTimestampExpiredForNode(Node nodeToCheck) {
        for(TimestampNode node : removedNodes) {
            if(nodeToCheck.equals(node)) {
                if(node.hasNodeExpired(30000)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Removes a node from the removed nodes list if it exists.
     * @param nodeToRemove - Node to remove
     */
    public void removeNodeFromList(Node nodeToRemove) {
        for(TimestampNode node: removedNodes) {
            if(nodeToRemove.equals(node)) {
                removedNodes.remove(node);
                break;
            }
        }
    }



}
