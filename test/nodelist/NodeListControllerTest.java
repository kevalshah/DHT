package nodelist;

import org.junit.Test;

import java.net.InetAddress;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class NodeListControllerTest {

    @Test
    public void testSetSuccessorListLessThanCapacity() throws Exception {
        NodeListController nlc = new NodeListController();
        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), 12, 12));
        expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), 13, 13));
        expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), 14, 14));
        nlc.setSuccessorList(expectedSuccessorList);

        // Assert length of successor list
        assertTrue(nlc.getSuccessorListLength() <= NodeListController.MAX_SUCCESSOR_LIST_LENGTH);
        assertEquals(3, nlc.getSuccessorListLength());

        // Assert successor list contents
        ArrayList<Node> actualSuccessorList = nlc.getSuccessorList();
        for(int i = 0; i < 3; i++) {
            assertEquals(expectedSuccessorList.get(i), actualSuccessorList.get(i));
        }
    }


    @Test
    public void testSetSuccessorListGreaterThanCapacity() throws Exception {
        NodeListController nlc = new NodeListController();
        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < NodeListController.MAX_SUCCESSOR_LIST_LENGTH + 1; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);

        // Assert length of successor list
        assertTrue(nlc.getSuccessorListLength() <= NodeListController.MAX_SUCCESSOR_LIST_LENGTH);
        assertEquals(NodeListController.MAX_SUCCESSOR_LIST_LENGTH, nlc.getSuccessorListLength());

        // Assert successor list contents
        ArrayList<Node> actualSuccessorList = nlc.getSuccessorList();
        for(int i = 0; i < NodeListController.MAX_SUCCESSOR_LIST_LENGTH; i++) {
            assertEquals(expectedSuccessorList.get(i), actualSuccessorList.get(i));
        }
    }


    @Test
    public void testGetSuccessorListWithSelf() throws Exception {
        NodeListController nlc = new NodeListController();
        Node self = new Node(InetAddress.getLocalHost(), 1000, 1000);
        nlc.setSelf(self);

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < NodeListController.MAX_SUCCESSOR_LIST_LENGTH; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);

        // Assert return list contents
        ArrayList<Node> actualList = nlc.getSuccessorListWithSelf();
        for(int i = 0; i < expectedSuccessorList.size(); i++) {
            if(i == 0) {
                assertEquals(self, actualList.get(i));
            } else {
                assertEquals(expectedSuccessorList.get(i), actualList.get(i+1));
            }
        }

    }


    @Test
    public void testGetSuccessorListWithPredecessorAndSelf() throws Exception {
        NodeListController nlc = new NodeListController();
        Node self = new Node(InetAddress.getLocalHost(), 1000, 1000);
        Node predecessor = new Node(InetAddress.getLocalHost(), 500, 500);
        nlc.setSelf(self);
        nlc.setPredecessor(predecessor);

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < NodeListController.MAX_SUCCESSOR_LIST_LENGTH; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);

        // Assert return list contents
        ArrayList<Node> actualList = nlc.getSuccessorListWithPredecessorAndSelf();
        for(int i = 0; i < expectedSuccessorList.size(); i++) {
            if(i == 0) {
                assertEquals(predecessor, actualList.get(i));
            } else if(i == 1) {
                assertEquals(self, actualList.get(i));
            } else {
                assertEquals(expectedSuccessorList.get(i), actualList.get(i+2));
            }
        }

    }


    @Test
    public void testGetSuccessorListWithNullPredecessorAndSelf() throws Exception {
        NodeListController nlc = new NodeListController();
        Node self = new Node(InetAddress.getLocalHost(), 1000, 1000);
        nlc.setSelf(self);
        nlc.setPredecessor(null);

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < NodeListController.MAX_SUCCESSOR_LIST_LENGTH; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);

        // Assert return list contents
        ArrayList<Node> actualList = nlc.getSuccessorListWithPredecessorAndSelf();
        for(int i = 0; i < expectedSuccessorList.size(); i++) {
            if(i == 0) {
                assertEquals(new Node(null, -1, -1), actualList.get(i));
            } else if(i == 1) {
                assertEquals(self, actualList.get(i));
            } else {
                assertEquals(expectedSuccessorList.get(i), actualList.get(i+2));
            }
        }

    }


    @Test
    public void testGetTopSuccessors() throws Exception {
        NodeListController nlc = new NodeListController();

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < 10; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);


        ArrayList<Node> actualList = nlc.getTopSuccessors(5);

        // Assert return list size
        assertEquals(5, actualList.size());

        // Assert return list contents
        for(int i = 0; i < 5; i++) {
            assertEquals(expectedSuccessorList.get(i), actualList.get(i));
        }

    }


    @Test
    public void testGetTopSuccessorsWithGreaterThanCapacityInputSize() throws Exception {
        NodeListController nlc = new NodeListController();

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < 10; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);


        ArrayList<Node> actualList = nlc.getTopSuccessors(11);

        // Assert return list size
        assertEquals(10, actualList.size());

        // Assert return list contents
        for(int i = 0; i < 10; i++) {
            assertEquals(expectedSuccessorList.get(i), actualList.get(i));
        }

    }


    @Test
    public void testGetFirstSuccessor() throws Exception {
        NodeListController nlc = new NodeListController();

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < 10; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);

        assertEquals(new Node(InetAddress.getLocalHost(), 0, 0), nlc.getFirstSuccessor());
    }


    @Test
    public void testAddFirstSuccessor() throws Exception {
        NodeListController nlc = new NodeListController();

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < 10; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);

        Node newFirstSuccessor = new Node(InetAddress.getLocalHost(), 1000, 1000);
        nlc.addFirstSuccessor(newFirstSuccessor);

        assertEquals(newFirstSuccessor, nlc.getFirstSuccessor());
    }


    @Test
    public void testAddFirstSuccessorWithFullSuccessorList() throws Exception {
        NodeListController nlc = new NodeListController();

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < NodeListController.MAX_SUCCESSOR_LIST_LENGTH; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);

        Node newFirstSuccessor = new Node(InetAddress.getLocalHost(), 1000, 1000);
        nlc.addFirstSuccessor(newFirstSuccessor);

        // Assert size of successor list is still MAX SUCCESSOR LIST LENGTH
        assertEquals(NodeListController.MAX_SUCCESSOR_LIST_LENGTH, nlc.getSuccessorListLength());

        // Assert first successor is as expected
        assertEquals(newFirstSuccessor, nlc.getFirstSuccessor());
    }


    @Test
    public void testRemoveFirstSuccessor() throws Exception {
        NodeListController nlc = new NodeListController();

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < 10; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);

        // Assert successor list length before remove
        assertEquals(10, nlc.getSuccessorListLength());

        nlc.removeFirstSuccessor();

        // Assert successor list length after remove
        assertEquals(9, nlc.getSuccessorListLength());

        // Assert successor list contents
        ArrayList<Node> actualSuccessorList = nlc.getSuccessorList();
        for(int i = 0; i < actualSuccessorList.size(); i++) {
            assertEquals(expectedSuccessorList.get(i+1), actualSuccessorList.get(i));
        }
    }


    @Test
    public void testRemoveFirstSuccessorWithEqualPredecessor() throws Exception {
        NodeListController nlc = new NodeListController();

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), 0, 0));
        nlc.setSuccessorList(expectedSuccessorList);

        Node predecessor = new Node(InetAddress.getLocalHost(), 0, 0);
        nlc.setPredecessor(predecessor);

        // Assert successor list length before remove
        assertEquals(1, nlc.getSuccessorListLength());

        nlc.removeFirstSuccessor();

        // Assert successor list length after remove
        assertEquals(0, nlc.getSuccessorListLength());

        // Assert that predecessor is null
        assertEquals(null, nlc.getPredecessor());

    }


    @Test
    public void testIsNodeAlreadyInView() throws Exception {
        NodeListController nlc = new NodeListController();

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < 10; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }
        nlc.setSuccessorList(expectedSuccessorList);

        nlc.setSelf(new Node(InetAddress.getLocalHost(), 1000, 1000));
        nlc.setPredecessor(new Node(InetAddress.getLocalHost(), 2000, 2000));

        // Node in successor list
        assertEquals(true, nlc.isNodeAlreadyInView(0));
        assertEquals(true, nlc.isNodeAlreadyInView(9));

        // Node is predecessor
        assertEquals(true, nlc.isNodeAlreadyInView(2000));

        // Node is self
        assertEquals(true, nlc.isNodeAlreadyInView(1000));

        // Node not in view
        assertEquals(false, nlc.isNodeAlreadyInView(20001));
    }


    @Test
    public void testGetLastSuccessor() throws Exception {

        NodeListController nlc = new NodeListController();

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < 10; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }

        nlc.setSuccessorList(expectedSuccessorList);

        Node expectedLastSuccessor = new Node(InetAddress.getLocalHost(), 9, 9);
        assertEquals(expectedLastSuccessor, nlc.getLastSuccessor());

    }


    @Test
    public void testGetNodeByID() throws Exception {

        NodeListController nlc = new NodeListController();

        ArrayList<Node> expectedSuccessorList = new ArrayList<Node>();
        for(int i = 0; i < 10; i++) {
            expectedSuccessorList.add(new Node(InetAddress.getLocalHost(), i, i));
        }

        nlc.setSuccessorList(expectedSuccessorList);

        // Node in list
        Node expectedNode = new Node(InetAddress.getLocalHost(), 1, 1);
        assertEquals(expectedNode, nlc.getNodeByID(1));

        // Node not in list
        assertEquals(null, nlc.getNodeByID(10000));

    }
}