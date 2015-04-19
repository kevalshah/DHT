package nodelist;

import message.Payload;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class NodeListController {

    private ArrayList<Node> successorList;

    private static NodeListController nodeListController;

    private ReentrantReadWriteLock successorListLock;

    private Node predecessor;
    private Node self;

    private ReentrantReadWriteLock predecessorLock;
    private ReentrantReadWriteLock selfLock;

    public final static int MAX_SUCCESSOR_LIST_LENGTH = 50;

    protected NodeListController() {
        successorListLock = new ReentrantReadWriteLock();
        predecessorLock = new ReentrantReadWriteLock();
        selfLock = new ReentrantReadWriteLock();
        successorList = new ArrayList<Node>();
    }

    public static NodeListController getInstance() {
        if(nodeListController == null) {
            nodeListController = new NodeListController();
        }

        return nodeListController;
    }


    /**
     * Gets the current successor list length
     * @return
     */
    public int getSuccessorListLength() {
        successorListLock.readLock().lock();
        try {
            return successorList.size();
        } finally {
            successorListLock.readLock().unlock();
        }
    }


    /**
     * Gets the predecessor node
     * @return
     */
    public Node getPredecessor() {
        predecessorLock.readLock().lock();
        try {
            return predecessor;
        } finally {
            predecessorLock.readLock().unlock();
        }
    }


    /**
     * Sets the predecessor node
     * @param newPredecessor
     */
    public void setPredecessor(Node newPredecessor) {
        predecessorLock.writeLock().lock();
        predecessor = newPredecessor;
        predecessorLock.writeLock().unlock();
    }



    public void removePredecessor(Node predecessorToRemove) {
        Node currentPredecessor = getPredecessor();

        // Only remove predecessor if current predecessor is equal to predecessor to remove
        if(predecessorToRemove != null && predecessorToRemove.equals(currentPredecessor)) {
            predecessorLock.writeLock().lock();
            setPredecessor(null);
            predecessorLock.writeLock().unlock();

            RemovedNodeList removedNodeList = RemovedNodeList.getInstance();
            if(removedNodeList.isNodeInRemovedList(predecessorToRemove)) {
                // removed node is in removed node list so reset that instance's timestamp
                removedNodeList.removeNodeFromList(predecessorToRemove);
                removedNodeList.addRemovedNode(predecessorToRemove);
            } else {
                removedNodeList.addRemovedNode(predecessorToRemove);
            }

            // Search if removed predecessor is in successor list, if so, remove it.
            successorListLock.writeLock().lock();
            try {
                for(Node node : this.successorList) {
                    if(node.equals(predecessorToRemove)) {
                        this.successorList.remove(node);
                    }
                }
            } finally {
                successorListLock.writeLock().unlock();
            }

        }


    }


    /**
     * Gets node that represents self
     * @return
     */
    public Node getSelf() {
        selfLock.readLock().lock();
        try {
            return self;
        } finally {
            selfLock.readLock().unlock();
        }
    }


    /**
     * Sets node that represents self
     * @param newSelf
     */
    public void setSelf(Node newSelf) {
        selfLock.writeLock().lock();
        self = newSelf;
        selfLock.writeLock().unlock();
    }


    /**
     * Gets successor list IDs
     * @return
     */
    public int[] getSuccessorListIDs() {
        successorListLock.readLock().lock();
        try {
            int[] successorListIDs = {};
            if(successorList.size() > 0) {
                successorListIDs = new int[successorList.size()];
                for(int i = 0; i < successorListIDs.length; i++) {
                    successorListIDs[i] = successorList.get(i).getId();
                }
            }

            return successorListIDs;
        } finally {
            successorListLock.readLock().unlock();
        }
    }


    /**
     * Gets a copy of the successor list
     * @return
     */
    public ArrayList<Node> getSuccessorList() {
        successorListLock.readLock().lock();
        try {
            ArrayList<Node> successorList = new ArrayList<Node>();
            for(Node node : this.successorList) {
                successorList.add(new Node(node));
            }
            return successorList;
        } finally {
            successorListLock.readLock().unlock();
        }
    }



    /**
     * Gets a list with self and successor list in that specific order.
     * @return
     */
    public ArrayList<Node> getSuccessorListWithSelf() {
        successorListLock.readLock().lock();
        try {
            ArrayList<Node> list = new ArrayList<Node>();
            for(Node node : successorList) {
                list.add(new Node(node));
            }
            list.add(0, new Node(self));
            return list;
        } finally {
            successorListLock.readLock().unlock();
        }
    }


    /**
     * Gets a list with predecessor, self, and successor list in that order.
     * If predecessor is null, will return a predecessor node with ID = -1, null hostname and port = -1.
     * @return
     */
    public ArrayList<Node> getSuccessorListWithPredecessorAndSelf() {
        successorListLock.readLock().lock();
        try {
            ArrayList<Node> list = new ArrayList<Node>();
            for(Node node : successorList) {
                list.add(new Node(node));
            }
            list.add(0, new Node(self));

            if(predecessor == null) {
                list.add(0, new Node(null, -1, -1));
            } else {
                list.add(0, new Node(predecessor));
            }

            return list;
        } finally {
            successorListLock.readLock().unlock();
        }
    }


    /**
     * Gets the first N successors where N represents the input parameter
     * @param N - number of successors to get
     * @return
     */
    public ArrayList<Node> getTopSuccessors(int N) {
        successorListLock.readLock().lock();
        try {
            ArrayList<Node> successorList = new ArrayList<Node>();

            // If input N is greater than current size, return all successors
            if(N >= this.successorList.size()) {
                for(Node node : this.successorList) {
                    successorList.add(new Node(node));
                }
            }
            // Otherwise return top N successors
            else {
                for(int i = 0; i < N; i++) {
                    successorList.add(new Node(this.successorList.get(i)));
                }
            }

            return successorList;

        } finally {
            successorListLock.readLock().unlock();
        }
    }


    /**
     * Sets the successor list
     * @param successorList - Successor list to set
     */
    public void setSuccessorList(ArrayList<Node> successorList) {
        successorListLock.writeLock().lock();

        RemovedNodeList removedNodeList = RemovedNodeList.getInstance();

        this.successorList = new ArrayList<Node>();
        int length = successorList.size() > MAX_SUCCESSOR_LIST_LENGTH ? MAX_SUCCESSOR_LIST_LENGTH : successorList.size();
        for(int i = 0; i < length; i++) {
            if(successorList.get(i).getId() != self.getId()) {
                if(removedNodeList.isNodeInRemovedList(successorList.get(i))) {
                    if(removedNodeList.isTimestampExpiredForNode(successorList.get(i))) {
                        this.successorList.add(new Node(successorList.get(i)));
                    }
                } else {
                    this.successorList.add(new Node(successorList.get(i)));
                }
            }
        }

        successorListLock.writeLock().unlock();
    }


    /**
     * Gets the first successor. Returns null if no successor.
     * @return
     */
    public Node getFirstSuccessor() {
        successorListLock.readLock().lock();
        try {
            if(successorList.size() > 0) {
                return new Node(successorList.get(0));
            }
            return null;
        } finally {
            successorListLock.readLock().unlock();
        }
    }


    /**
     * Gets the last successor. Returns null if successor list is empty.
     * @return
     */
    public Node getLastSuccessor() {
        successorListLock.readLock().lock();
        try {
            if(successorList.size() > 0) {
                return new Node(successorList.get(successorList.size() - 1));
            }
            return null;
        } finally {
            successorListLock.readLock().unlock();
        }
    }


    /**
     * Adds a first successor.
     * @param node - Node to add as first successor
     */
    public void addFirstSuccessor(Node node) {
        successorListLock.writeLock().lock();
        try {
            successorList.add(0, node);
            if(successorList.size() > MAX_SUCCESSOR_LIST_LENGTH) {
                successorList.remove(MAX_SUCCESSOR_LIST_LENGTH);
            }
        } finally {
            successorListLock.writeLock().unlock();
        }
    }


    /**
     * Removes the first immediate successor from the successor list.
     * If predecessor also happens to be the first immediate successor, then set it to null.
     */
    public void removeFirstSuccessor() {
        int firstSuccessorID = -1;
        Node removedNode = null;

        successorListLock.writeLock().lock();
        if (successorList.size() > 0){
            firstSuccessorID = successorList.get(0).getId();
            removedNode = successorList.remove(0);
        }
        successorListLock.writeLock().unlock();

        predecessorLock.writeLock().lock();
        if (predecessor != null && predecessor.getId() == firstSuccessorID) {
            predecessor = null;
        }
        predecessorLock.writeLock().unlock();

        RemovedNodeList removedNodeList = RemovedNodeList.getInstance();
        if(removedNode != null) {

            if(removedNodeList.isNodeInRemovedList(removedNode)) {
                // removed node is in removed node list so reset that instance's timestamp
                removedNodeList.removeNodeFromList(removedNode);
                removedNodeList.addRemovedNode(removedNode);
            } else {
                removedNodeList.addRemovedNode(removedNode);
            }
        }
    }


    /**
     * Gets the node by input ID from successor list. If no such node, returns null.
     * @param nodeID -ID of node to fetch from successor list
     * @return
     */
    public Node getNodeByID(int nodeID) {
        successorListLock.readLock().lock();
        try {
            if(successorList.size() > 0) {
                for(Node node : successorList) {
                    if(node.getId() == nodeID) {
                        return node;
                    }
                }
            }
            return null;
        } finally {
            successorListLock.readLock().unlock();
        }
    }



    /**
     * Checks if the input node ID already exists in this node's view (self, predecessor, successor list)
     * @param nodeID - Node ID to check for
     * @return - Boolean representing whether the input node is in this node's view
     */
    public boolean isNodeAlreadyInView(int nodeID) {

        // Check predecessor
        predecessorLock.readLock().lock();
        try {
            if(predecessor != null) {
                if(predecessor.getId() == nodeID) {
                    return true;
                }
            }
        } finally {
            predecessorLock.readLock().unlock();
        }

        // Check self
        if(self != null && nodeID == self.getId()) {
            return true;
        }

        // Check successor list
        successorListLock.readLock().lock();
        try {
            if(successorList.size() > 0) {
                for(Node node : successorList) {
                    if(node.getId() == nodeID) {
                        return true;
                    }
                }
            }
            return false;
        } finally {
            successorListLock.readLock().unlock();
        }
    }


    /**
     * Prints self, predecessor, and successor list information.
     */
    public void printView() {
        Node temp = this.getPredecessor();

        // Print predecessor
        if(temp != null) {
            System.out.println("Predecessor: " + temp);
        }
        else {
            System.out.println("Predecessor: -");
        }

        // Print self
        temp = this.getSelf();
        if (temp != null) {
            System.out.println("Self: " + temp);
        } else {
            System.out.println("Self: -");
        }

        // Print successor list
        ArrayList<Node> temp_list = this.getSuccessorList();
        for (int i = 0; i < temp_list.size(); i++){
            System.out.println((i+1) + ") " + temp_list.get(i));
        }
    }

}