package algorithm;

import org.junit.Test;

import static org.junit.Assert.*;

public class ImmediateSuccessorRouterTest {

    @Test
    public void testIsInRange() throws Exception {

        // First ID < Request ID < Second ID
        assertEquals(true, ImmediateSuccessorRouter.isInRange(10, 5, 20));

        // First ID < Request ID <= Second ID
        assertEquals(true, ImmediateSuccessorRouter.isInRange(10, 5, 10));

        // First ID < Request ID <= Second ID
        assertEquals(true, ImmediateSuccessorRouter.isInRange(10, 5, 10));

        // Second ID < First ID < Request ID
        assertEquals(true, ImmediateSuccessorRouter.isInRange(205, 200, 10));

        // Second ID < First ID <= Request ID
        assertEquals(true, ImmediateSuccessorRouter.isInRange(200, 200, 10));

        // Request ID < Second ID < First ID
        assertEquals(true, ImmediateSuccessorRouter.isInRange(1, 20, 10));

        // Request ID <= Second ID < First ID
        assertEquals(true, ImmediateSuccessorRouter.isInRange(10, 20, 10));

        // Not in range
        assertEquals(false, ImmediateSuccessorRouter.isInRange(10, 0, 5));
        assertEquals(false, ImmediateSuccessorRouter.isInRange(15, 20, 10));

    }


    @Test(expected = IllegalArgumentException.class)
    public void testIsInRangeInvalidParams() throws Exception {
        ImmediateSuccessorRouter.isInRange(-1, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsInRangeInvalidParams2() throws Exception {
        ImmediateSuccessorRouter.isInRange(-1, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsInRangeInvalidParams3() throws Exception {
        ImmediateSuccessorRouter.isInRange(-1, -1, -1);
    }


    @Test
    public void testisSelfPotentialIMS() throws Exception {
        // True cases
        assertEquals(true, ImmediateSuccessorRouter.isSelfPotentialIMS(10, 9, 11));
        assertEquals(true, ImmediateSuccessorRouter.isSelfPotentialIMS(201, 200, 11));
        assertEquals(true, ImmediateSuccessorRouter.isSelfPotentialIMS(0, 200, 11));

        // False cases
        assertEquals(false, ImmediateSuccessorRouter.isSelfPotentialIMS(10, 10, 11));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testIsSelfPotentialIMSInvlaidParams() throws Exception {
        ImmediateSuccessorRouter.isSelfPotentialIMS(-1, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSelfPotentialIMSInvlaidParams2() throws Exception {
        ImmediateSuccessorRouter.isSelfPotentialIMS(-1, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSelfPotentialIMSInvlaidParams3() throws Exception {
        ImmediateSuccessorRouter.isSelfPotentialIMS(-1, -1, -1);
    }


    @Test
    public void testFindPotentialIMS() throws Exception {

        int[] successorList = {3, 5, 7};
        // Potential IMS found in successor list (ascending)
        assertEquals(3, ImmediateSuccessorRouter.findPotentialIMS(2, -1, 1, successorList));
        assertEquals(5, ImmediateSuccessorRouter.findPotentialIMS(4, -1, 1, successorList));
        assertEquals(7, ImmediateSuccessorRouter.findPotentialIMS(6, -1, 1, successorList));
        assertEquals(3, ImmediateSuccessorRouter.findPotentialIMS(3, -1, 1, successorList));
        assertEquals(5, ImmediateSuccessorRouter.findPotentialIMS(5, -1, 1, successorList));
        assertEquals(7, ImmediateSuccessorRouter.findPotentialIMS(7, -1, 1, successorList));

        int[] successorList2 = {10, 50, 2};
        // Potential IMS found in successor list (not ascending)
        assertEquals(10, ImmediateSuccessorRouter.findPotentialIMS(7, -1, 5, successorList2));
        assertEquals(50, ImmediateSuccessorRouter.findPotentialIMS(20, -1, 5, successorList2));
        assertEquals(2, ImmediateSuccessorRouter.findPotentialIMS(51, -1, 5, successorList2));
        assertEquals(10, ImmediateSuccessorRouter.findPotentialIMS(10, -1, 5, successorList2));
        assertEquals(50, ImmediateSuccessorRouter.findPotentialIMS(50, -1, 5, successorList2));
        assertEquals(2, ImmediateSuccessorRouter.findPotentialIMS(2, -1, 5, successorList2));

        // Self found as potential IMS
        assertEquals(5, ImmediateSuccessorRouter.findPotentialIMS(4, 3, 5, successorList2));

    }


    @Test(expected = NoPotentialIMSException.class)
    public void testFindPotentialIMSForNoPotentialIMSException() throws Exception {
        int[] successorList = {10, 50, 2};
        ImmediateSuccessorRouter.findPotentialIMS(3, 3, 5, successorList);
    }

    @Test(expected = NoPotentialIMSException.class)
    public void testFindPotentialIMSForNoPotentialIMSException2() throws Exception {
        int[] successorList = {10, 50, 0};
        ImmediateSuccessorRouter.findPotentialIMS(1, 3, 5, successorList);
    }


    @Test(expected = NoPotentialIMSException.class)
    public void testFindPotentialIMSForNoPotentialIMSException3() throws Exception {
        int[] successorList = {10, 50, 0};
        ImmediateSuccessorRouter.findPotentialIMS(1, -1, 5, successorList);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testFindPotentialIMSInvlaidParams() throws Exception {
        int[] succList = {23};
        ImmediateSuccessorRouter.findPotentialIMS(-1, 0, 0, succList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindPotentialIMSInvlaidParams2() throws Exception {
        int[] succList = {};
        ImmediateSuccessorRouter.findPotentialIMS(0, 12, 0, succList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindPotentialIMSInvlaidParams3() throws Exception {
        ImmediateSuccessorRouter.findPotentialIMS(0, 12, 0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindPotentialIMSInvlaidParams4() throws Exception {
        int[] succList = {};
        ImmediateSuccessorRouter.findPotentialIMS(0, -1, 0, succList);
    }



}