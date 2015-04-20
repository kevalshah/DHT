package main;


import command.RequestCodes;
import message.*;
import utility.HashUtility;
import utility.Logger;
import utility.UTF8StringUtility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

public class Client {

    private static boolean DEBUG = false;
    private final static int RETRY_LIMIT = 3;
    private final static int DEFAULT_TIMEOUT_MS = 1000;

    private static long aveTurnaroundTime;
    private static int totalSuccessfulCall;

    public static InetAddress serverAddr;

    public static int serverPort;


    protected static void setServerAddr(InetAddress serverAddr) {
        Client.serverAddr = serverAddr;
    }

    protected static void setServerPort(int serverPort) {
        Client.serverPort = serverPort;
    }

    private static void printArgumentErrorMessage() {
        System.out.println(
                "Usage: The following arguments are needed in the following order: <server hostname> <server port> <d or D for Debugging>(Optional)");
    }

    private static void printUnknownHostErrorMessage(String hostname) {
        System.out.println("Error: The following hostname, " + hostname + " could not be resolved.");
    }

    private static void printInvalidPortErrorMessage(String port) {
        System.out.println("Error: The following port, " + port + " is invalid.");
    }


    private static void printHelpUsage() {
        System.out.println();
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("HELP - USAGE INFORMATION");
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("put <Key> <Value> -- Attempt to put key and value in server store");
        System.out.println("                  -- <Key> - MAX Chars. = 32");
        System.out.println("                  -- <Value> MAX Chars. = 15000");
        System.out.println("                  -- Example Usage: put key value");
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("get <Key>         -- Attempts to get value of corresponding key from server store");
        System.out.println("                  -- <Key> - MAX Chars. = 32");
        System.out.println("                  -- Example Usage: get key");
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("remove <Key>      -- Attempts to remove key-value pair associated with this key from server store");
        System.out.println("                  -- <Key> - MAX Chars. = 32");
        System.out.println("                  -- Example Usage: remove key");
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("shutdown          -- Sends a shutdown message to server node");
        System.out.println("                  -- Example Usage: shutdown");
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("nodelist          -- Requests server node for its node list");
        System.out.println("                  -- Example Usage: nodelist");
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("server <address> <port> -- Chages the contact server to communicate to");
        System.out.println("                        -- Example Usage: changeserver planetlab4.williams.edu 51612");
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("printserver       -- Prints the current contact server");
        System.out.println("                  -- Example Usage: printserver");
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("exit              -- Quit Program");
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println();
    }


    /**
     * Builds a request message with an unknown request code and sends to server, and waits for reply.
     * @throws java.io.IOException
     * @throws message.InvalidMessageException
     */
    public static void unknownCommand() throws IOException, InvalidMessageException {

        // Generate a random byte in the range of the min unused command value to the max unused command value
        Random rand = new Random();
        int maxUnusedCommandValue = 127;
        int minUnusedCommandValue = 5;
        int randomNum = rand.nextInt((maxUnusedCommandValue - minUnusedCommandValue) + 1) + minUnusedCommandValue;

        byte reqcode = (byte)randomNum;

        byte[] randomPayload = new byte[16000];
        rand.nextBytes(randomPayload);

        randomPayload[0] = reqcode;

        byte[] messageHeader;
        byte[] msg;

        messageHeader = Header.buildMessageHeader((short) 0);

        msg = Message.buildMessage(messageHeader, randomPayload);

        try {
            byte[] responseMessage = connect(serverAddr, serverPort, msg);
            Logger.printResponseMessage(responseMessage, false);
            System.out.println();
        } catch(RetriesExhaustedException e) {
            System.out.println("\nConnection failed: No response from server\n");
        }
    }


    /**
     * Change the contact server address and port
     * @param address - New contact server address
     * @param port - New contact server receiving port
     */
    public static void changeContactServer(String address, int port) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            serverAddr =inetAddress;

            if(port <= 0) {
                throw new IllegalArgumentException();
            } else {
                serverPort = port;
            }

        } catch(UnknownHostException e) {
            throw new IllegalArgumentException();
        }

        System.out.print("New Contact Server: ");
        System.out.print(serverAddr.getHostName() + " ");
        System.out.print("PORT: " + serverPort + "\n\n");
    }




    /**
     * Sends a node list request message
     * @throws java.io.IOException
     * @throws message.InvalidMessageException
     */
    public static void nodeListRequest() throws IOException, InvalidMessageException {
        byte[] messageHeader;
        byte[] msg;
        byte[] payload;

        messageHeader = Header.buildMessageHeader();

        payload = Payload.buildPayloadWithOnlyCommand(RequestCodes.NODE_LIST_REQUEST);

        msg = Message.buildMessage(messageHeader, payload);

        try {
            byte[] responseMessage = connect(serverAddr, serverPort, msg);
            Logger.printResponseMessage(responseMessage, false);
            System.out.println();
        } catch(RetriesExhaustedException e) {
            System.out.println("\nConnection failed: No response from server\n");
        }
    }



    /**
     * Builds a SHUTDOWN request message and sends to server, and waits for reply.
     * @throws java.io.IOException
     * @throws message.InvalidMessageException
     */
    public static void shutdown() throws IOException, InvalidMessageException {
        byte[] messageHeader;
        byte[] msg;
        byte[] payload;

        messageHeader = Header.buildMessageHeader((short)0);

        payload = Payload.buildPayloadWithOnlyCommand(RequestCodes.SHUTDOWN);

        msg = Message.buildMessage(messageHeader, payload);

        try {
            byte[] responseMessage = connect(serverAddr, serverPort, msg);
            Logger.printResponseMessage(responseMessage, false);
            System.out.println();
        } catch(RetriesExhaustedException e) {
            System.out.println("\nConnection failed: No response from server\n");
        }
    }


    /**
     * Builds a PUT request message and sends to server, and waits for reply.
     * @param key - Key of key-value pair to put in server store
     * @param value - Value of key-value pair to put in server store
     * @throws java.io.IOException
     * @throws message.InvalidMessageException
     */
    public static void put(byte[] key, byte[] value) throws IOException, InvalidMessageException {
        byte[] messageHeader;
        byte[] msg;
        byte[] payload;

        messageHeader = Header.buildMessageHeader((short)0);

        payload = Payload.buildStandardRequestPayload(RequestCodes.PUT, key, value);

        msg = Message.buildMessage(messageHeader, payload);

        try {
            byte[] responseMessage = connect(serverAddr, serverPort, msg);
            Logger.printResponseMessage(responseMessage, false);
            System.out.println();
        } catch(RetriesExhaustedException e) {
            System.out.println("\nConnection failed: No response from server\n");
        }
    }


    /**
     * Builds a GET request message and sends to server, and waits for reply.
     * @param key - Key of GET operation
     * @throws java.io.IOException
     * @throws message.InvalidMessageException
     */
    public static void get(byte[] key) throws IOException, InvalidMessageException {
        byte[] messageHeader;
        byte[] msg;
        byte[] payload;
        messageHeader = Header.buildMessageHeader((short)0);

        payload = Payload.buildStandardRequestPayload(RequestCodes.GET, key);

        msg = Message.buildMessage(messageHeader, payload);

        try {
            byte[] responseMessage = connect(serverAddr, serverPort, msg);
            Logger.printResponseMessage(responseMessage, true);
            System.out.println();

        } catch(RetriesExhaustedException e) {
            System.out.println("\nConnection failed: No response from server\n");
        }
    }


    /**
     * Builds a REMOVE request message and sends to server, and waits for reply.
     * @param key - Key of REMOVE operation
     * @throws java.io.IOException
     * @throws message.InvalidMessageException
     */
    public static void remove(byte[] key) throws IOException, InvalidMessageException {
        byte[] messageHeader;
        byte[] msg;
        byte[] payload;

        messageHeader = Header.buildMessageHeader((short)0);

        payload = Payload.buildStandardRequestPayload(RequestCodes.REMOVE, key);

        msg = Message.buildMessage(messageHeader, payload);

        try {
            byte[] responseMessage = connect(serverAddr, serverPort, msg);
            Logger.printResponseMessage(responseMessage, false);
            System.out.println();
        } catch(RetriesExhaustedException e) {
            System.out.println("\nConnection failed: No response from server\n");
        }
    }


    /**
     * Sends a message to a destination server and awaits response message.
     * Will attempt a certain number of retries, if all failed then will throw an IOException
     * @param serverAddress - Address of server
     * @param serverPort    - Receiving port of server
     * @param message       - Message to send
     * @return - Byte array representing response message from server
     * @throws message.InvalidMessageException - Thrown if message to send has an invalid format
     * @throws java.io.IOException             - Thrown if there was a problem in connecting socket or all retries were exhausted
     */
    private static byte[] connect(InetAddress serverAddress, int serverPort, byte[] message)
            throws InvalidMessageException, IOException, RetriesExhaustedException {

        System.out.println("\nSending message (" + Header.convertToString(message) + ") to host " + serverAddress.getHostName() + "...");

        DatagramSocket socket = new DatagramSocket();

        byte[] uniqueID = Message.getBytesInRange(message, 0, Header.HEADER_SIZE_BYTES);

        byte[] rcvData = null;

        socket.setSoTimeout(DEFAULT_TIMEOUT_MS);

        // Keep on resending message if timeouts occur until retry attempt limit is reached or there is a reply from server
        int retryCount = 0;
        long startTime = System.nanoTime();
        while(retryCount <= RETRY_LIMIT) {
            try {
                DatagramPacket sendPacket = new DatagramPacket(message, message.length, serverAddress, serverPort);
                socket.send(sendPacket);

                rcvData = new byte[17000];
                DatagramPacket rcvPacket = new DatagramPacket(rcvData, rcvData.length);
                socket.receive(rcvPacket);

                /* Execution will only reach this point if a message was returned from the server */

                // Compare received unique ID and sent unique ID
                ByteBuffer receivedID = ByteBuffer.allocate(Header.UNIQUE_ID_SIZE_BYTES);
                receivedID.order(ByteOrder.LITTLE_ENDIAN);
                receivedID.put(rcvData, 0, Header.UNIQUE_ID_SIZE_BYTES);
                boolean uniqueIDMatch = Arrays.equals(receivedID.array(), uniqueID);

                // Break from loop as message was successfully received from server with correct unique ID
                if(uniqueIDMatch) {
                    break;
                }

                // ID mismatch
                if(DEBUG) {
                    System.out.println("Unique ID mismatch. Resending message...\n");
                }

            } catch(SocketTimeoutException e) {
                // Timeout occurred
                if(DEBUG) {
                    System.out.println("Timeout occurred. Resending message...\n");
                }
            }

            /* Execution only gets to this point if a timeout occurred or there was a mismatch in the ID in the sent message and received message */

            // Increment retry attempt count
            retryCount++;

            // Double timeout period
            socket.setSoTimeout(socket.getSoTimeout() * 2);
        }
        long endTime = System.nanoTime();
        // All retry attempts failed, so throw exception
        if(retryCount > RETRY_LIMIT) {
            throw new RetriesExhaustedException("Operation failed: Number of retries expired");
        }
        long turnAroundTime = endTime - startTime;
        aveTurnaroundTime = (aveTurnaroundTime*totalSuccessfulCall + turnAroundTime)/(totalSuccessfulCall + 1);
        totalSuccessfulCall++;
        System.out.println("Response received. Total turnaround time: " + ((float)((endTime - startTime)/1000000)) + " milliseconds, accumulated average time: " + ((float)(aveTurnaroundTime/1000000)) + " milliseconds.");
        return rcvData;
    }



    public final static int MAX_KEY_SIZE_BYTES = Payload.KEY_SIZE_BYTES;

    /**
     * Converts key in string form to equivalent byte array.
     * If key length is less than MAX KEY SIZE then '\0' will be appended
     * to the key until the key length is MAX KEY SIZE.
     * @param key - Key in string format
     * @return - Byte array representing key. Will return null if key is empty, null,
     *           contains the character '0' or exceeds MAX KEY SIZE
     */
    public static byte[] convertKeyToByteRepresentation(String key) {

        if(key == null || key.isEmpty() || key.contains("\0") || key.length() > MAX_KEY_SIZE_BYTES) {
            return null;
        }

        if(key.length() == MAX_KEY_SIZE_BYTES) {
            return UTF8StringUtility.stringToBytesUTF8(key);
        }

        String modifiedKey = key;

        for(int i = 0; i < MAX_KEY_SIZE_BYTES - key.length(); i++) {
            modifiedKey += "\0";
        }

        return UTF8StringUtility.stringToBytesUTF8(modifiedKey);
    }






    public static void main(String[] args) throws IOException {

        serverAddr = null;

        // Command line arguments validation
        // Command line arguments format: <server hostname> <server receiving port> <d or D to enable debugging mode>(Optional)
        if(args.length >= 2) {
            try {
                serverAddr = InetAddress.getByName(args[0]);
                serverPort = Integer.parseInt(args[1]);
            } catch(UnknownHostException e) {
                printUnknownHostErrorMessage(args[0]);
                return;
            } catch(NumberFormatException e) {
                printInvalidPortErrorMessage(args[1]);
                return;
            }

            if(args.length == 3) {
                if(args[2].equals("d") || args[2].equals("D")) {
                    DEBUG = true;
                }
            }
        } else {
            printArgumentErrorMessage();
            return;
        }


        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader input = new BufferedReader(in);

        String user_input;

        System.out.println("DHT Client is now running...");

        System.out.println("Type -h for usage information\n");

        try {
            while(true) {

                System.out.print("DHT_Client: ");
                user_input = input.readLine();

                String[] command = user_input.split("\\s+");

                if(command[0].equals("-h")) {
                    printHelpUsage();
                } else if(command[0].equals("put")) {
                    if(command.length != 3) {
                        System.out.println("ERROR: Incorrect usage of 'put'. Type -h for usage information.");
                    } else {

                        String key = command[1];
                        String value = command[2];

                        if(key.length() > 32) {
                            System.out.println("ERROR: Key size exceeds maximum size of 32 characters. Type -h for usage information.");
                            continue;
                        }

                        if(value.length() > 15000) {
                            System.out.println("ERROR: Value size exceeds maximum size of 15000 characters. Type -h for usage information.");
                            continue;
                        }

                        try {
                            System.out.println("KEY ID: " + HashUtility.simpleHash(key, HashUtility.DEFAULT_HASH_RANGE));
                            put(convertKeyToByteRepresentation(
                                    key), UTF8StringUtility.stringToBytesUTF8(value));
                        } catch(InvalidMessageException e) {
                            System.out.println("ERROR: Client-side error. Operation could not complete successfully.");
                        }
                    }

                } else if(command[0].equals("get")) {
                    if(command.length != 2) {
                        System.out.println("ERROR: Incorrect usage of 'get'. Type -h for usage information.");
                    } else {
                        String key = command[1];

                        if(key.length() > 32) {
                            System.out.println("ERROR: Key size exceeds maximum size of 32 characters. Type -h for usage information.");
                            continue;
                        }

                        try {
                            System.out.println("KEY ID: " + HashUtility.simpleHash(key, HashUtility.DEFAULT_HASH_RANGE));
                            get(convertKeyToByteRepresentation(key));
                        } catch(InvalidMessageException e) {
                            System.out.println("ERROR: Client-side error. Operation could not complete successfully.");
                        }
                    }
                } else if(command[0].equals("remove")) {
                    if(command.length != 2) {
                        System.out.println("ERROR: Incorrect usage of 'remove'. Type -h for usage information.");
                    } else {
                        String key = command[1];

                        if(key.length() > 32) {
                            System.out.println("ERROR: Key size exceeds maximum size of 32 characters. Type -h for usage information.");
                            continue;
                        }

                        try {
                            System.out.println("KEY ID: " + HashUtility.simpleHash(key, HashUtility.DEFAULT_HASH_RANGE));
                            remove(convertKeyToByteRepresentation(key));
                        } catch(InvalidMessageException e) {
                            System.out.println("ERROR: Client-side error. Operation could not complete successfully.");
                        }
                    }
                } else if(command[0].equals("shutdown")) {
                    try {
                        shutdown();
                    } catch(InvalidMessageException e) {
                        System.out.println("ERROR: Client-side error. Operation could not complete successfully.");
                    }
                } else if(command[0].equals("nodelist")) {
                    try {
                        nodeListRequest();
                    } catch(InvalidMessageException e) {
                        System.out.println("ERROR: Client-side error. Operation could not complete successfully.");
                    }
                } else if(command[0].equals("server")) {
                    if(command.length != 3) {
                        System.out.println("ERROR: Incorrect usage of 'server'. Type -h for usage information.");
                    } else {
                        String address = command[1];
                        int port = 0;
                        try {
                            port = Integer.parseInt(command[2]);
                        } catch(NumberFormatException e) {
                            System.out.println("sERROR: Incorrect port format. Type -h for usage information.");
                            continue;
                        }

                        try {
                            changeContactServer(address, port);
                        } catch(IllegalArgumentException e) {
                            System.out.println("ERROR: Unknown host or invalid port specified. Type -h for usage information.");
                        }
                    }
                } else if(command[0].equals("printserver")) {
                    System.out.print("Contact Server: ");
                    System.out.print(serverAddr.getHostName() + " ");
                    System.out.print("PORT: " + serverPort + "\n\n");
                } else if(command[0].equals("exit")) {
                    break;
                } else {
                    if(DEBUG) {
                        try {
                            unknownCommand();
                        } catch(InvalidMessageException e) {
                            System.out.println("ERROR: Client-side error. Operation could not complete successfully.");
                        }
                    } else {
                        System.out.println("Unrecognized command. Type -h for usage information.");
                    }
                }

            }
        } catch(IOException e) {
            System.out.println("ERROR: Client-side error detected. Terminating program.");
            e.printStackTrace();
        } finally {

            try {
                in.close();
                input.close();
            } catch(IOException e) {
                e.printStackTrace();
            }

        }

    }






}
