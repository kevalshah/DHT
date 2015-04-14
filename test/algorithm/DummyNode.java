package algorithm;

/**
 * Represents a dummy node (self ID, predecessor ID, successor list)
 */
public class DummyNode {

    public int selfID;
    public int predecessorID;
    public int[] successorList;

    public DummyNode(int predecessorID, int selfID, int[] successorList) {
        this.selfID = selfID;
        this.predecessorID = predecessorID;
        this.successorList = successorList;
    }
}
