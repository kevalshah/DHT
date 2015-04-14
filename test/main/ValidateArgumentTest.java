package main;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValidateArgumentTest {

    @Test(expected = IllegalArgumentException.class)
    public void testValidateArgsWithInvalidArgs() throws Exception {
        String[] args = {"p", "s", "d", "s"};
        Server.validateArgs(args);
    }

    @Test
    public void testValidateArgsWithValidLocalPortOnly() throws Exception {
        String[] args = {"-p", "123"};
        Server.validateArgs(args);
        assertEquals(123, Server.localPort);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testValidateArgsWithInvalidLocalPortOnly() throws Exception {
        String[] args = {"-p", "-1"};
        Server.validateArgs(args);
    }


    @Test
    public void testValidateArgsWithValidNodeIDOnly() throws Exception {
        String[] args = {"-n", "123"};
        Server.validateArgs(args);
        assertEquals(123, Server.nodeID);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testValidateArgsWithInvalidNodeIDOnly() throws Exception {
        String[] args = {"-n", "-1"};
        Server.validateArgs(args);
        assertEquals(123, Server.nodeID);
    }


    @Test
    public void testValidateArgsWithValidContactNodeOnly() throws Exception {
        String[] args = {"-c", "localhost:51612"};
        Server.validateArgs(args);
        assertEquals("localhost", Server.contactNodeString);
        assertEquals(51612, Server.contactNodePort);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testValidateArgsWithInvalidContactNodePortOnly() throws Exception {
        String[] args = {"-c", "localhost:-1"};
        Server.validateArgs(args);
        assertEquals("localhost", Server.contactNodeString);
        assertEquals(51612, Server.contactNodePort);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testValidateArgsWithInvalidContactNodeFormatOnly() throws Exception {
        String[] args = {"-c", "localhost"};
        Server.validateArgs(args);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testValidateArgsWithInvalidContactNodeFormatOnly2() throws Exception {
        String[] args = {"-c", ":"};
        Server.validateArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateArgsWithInvalidContactNodeFormatOnly3() throws Exception {
        String[] args = {"-c", ":123"};
        Server.validateArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateArgsWithInvalidContactNodeFormatOnly4() throws Exception {
        String[] args = {"-c", "localhost:"};
        Server.validateArgs(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateArgsWithInvalidContactNodeFormatOnly5() throws Exception {
        String[] args = {"-c", ""};
        Server.validateArgs(args);
    }


    @Test
    public void testValidateArgsWithParamsOutOfOrder() throws Exception {
        String[] args = {"-n", "150", "-p", "123", "-c", "localhost:1234"};
        Server.validateArgs(args);
        assertEquals(150, Server.nodeID);
        assertEquals(123, Server.localPort);
        assertEquals("localhost", Server.contactNodeString);
        assertEquals(1234, Server.contactNodePort);
    }




}