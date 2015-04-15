package main;

import nodelist.Node;
import nodelist.NodeListController;
import protocol.JoinInitiator;
import protocol.PredecessorCheckInitiator;
import protocol.SuccessorCheckInitiator;
import protocol.UDPReceive;
import utility.HashUtility;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Server {

    // Global variables
    public static InetAddress contactNodeAddress = null;
    public static int contactNodePort = -1;
    public static int localPort = -1;
    public static int nodeID = -1;

    // Package private variables
    static String contactNodeString = null;
    static DatagramSocket socket;

    public static void main(String[] args) {

        if(args.length > 0) {
            try {
                validateArgs(args);
            } catch(IllegalArgumentException e) {
                printUsageMessageAndExit();
            }
        }

        // Check if contact node was provided
        if(contactNodeString != null) {
            try {
                contactNodeAddress = InetAddress.getByName(contactNodeString);
            } catch(UnknownHostException e) {
                System.err.println("ERROR: Invalid contact hostname specified");
                printUsageMessageAndExit();
            }
        }

        // Check if node ID was provided. If not, hash local hostname and assign ID
        if(nodeID < 0) {
            try {
                nodeID = HashUtility.hashString(InetAddress.getLocalHost().getHostName());
            } catch(UnknownHostException e) {
                System.err.println("ERROR: Could not assign a node ID");
                System.exit(-1);
            }
        }

        // Create socket based on whether local port was set or not
        try {
            if(localPort >= 0) {
                socket = new DatagramSocket(localPort);
            } else {
                socket = new DatagramSocket();
                localPort = socket.getLocalPort();
            }
        } catch(SocketException e) {
            System.err.println("ERROR: Could not bind to local port");
            printUsageMessageAndExit();
        }

        // Create self node
        NodeListController nlc = NodeListController.getInstance();
        try {
            nlc.setSelf(new Node(InetAddress.getLocalHost(), localPort, nodeID));
        } catch(UnknownHostException e) {
            System.err.println("ERROR: Could not create self node");
        }

        // Create receiving thread and scheduler for it
        UDPReceive receiverThread = new UDPReceive(socket);
        ScheduledExecutorService UDPReceiveScheduler = Executors.newScheduledThreadPool(1);
        final ScheduledFuture<?> receiverThreadHandle = UDPReceiveScheduler.scheduleWithFixedDelay(receiverThread, 0, 10, TimeUnit.MILLISECONDS);

        // Create join initiator thread and scheduler for it
        JoinInitiator joinInitiatorThread = new JoinInitiator();
        ScheduledExecutorService joinInitiatorScheduler = Executors.newScheduledThreadPool(1);
        final ScheduledFuture<?> joinInitiatorHandle = joinInitiatorScheduler.scheduleWithFixedDelay(joinInitiatorThread, 0, 10, TimeUnit.MILLISECONDS);

        // Create predecessor check initiator thread and scheduler for it
        PredecessorCheckInitiator predecessorCheckInitiatorThread = new PredecessorCheckInitiator();
        ScheduledExecutorService predecessorCheckInitiatorScheduler = Executors.newScheduledThreadPool(1);
        final ScheduledFuture<?> predecessorCheckInitiatorHandle = predecessorCheckInitiatorScheduler.scheduleWithFixedDelay(predecessorCheckInitiatorThread, 0, 10, TimeUnit.MILLISECONDS);

        // Create successor check initiator thread and scheduler for it
        SuccessorCheckInitiator successorCheckInitiatorThread = new SuccessorCheckInitiator();
        ScheduledExecutorService successorCheckInitiatorScheduler = Executors.newScheduledThreadPool(1);
        final ScheduledFuture<?> successorCheckInitiatorHandle = successorCheckInitiatorScheduler.scheduleWithFixedDelay(successorCheckInitiatorThread, 0, 10, TimeUnit.MILLISECONDS);

    }


    /**
     * Validates input arguments
     * @param args - Input arguments
     * @throws java.lang.IllegalArgumentException if and invalid argument is specified
     */
    public static void validateArgs(String[] args) throws IllegalArgumentException {

        // Check if argument count is even. If not, throw exception.
        if(args.length % 2 != 0) {
            throw new IllegalArgumentException();
        }

        for(int i = 0; i < args.length; i++) {
            if(i % 2 == 0) {
                // Local port argument
                if(args[i].equals("-p")) {
                    try {
                        localPort = Integer.parseInt(args[i + 1]);
                        if(localPort < 0) {
                            throw new Exception();
                        }
                    } catch(Exception e) {
                        System.err.println("ERROR: Invalid local port specified");
                        throw new IllegalArgumentException();
                    }
                }
                // Node id argument
                else if(args[i].equals("-n")) {
                    try {
                        nodeID = Integer.parseInt(args[i + 1]);
                        if(nodeID < 0) {
                            throw new Exception();
                        }
                    } catch(Exception e) {
                        System.err.println("ERROR: Invalid node ID specified. Must be >= 0.");
                        throw new IllegalArgumentException();
                    }

                }
                // Contact node argument
                else if(args[i].equals("-c")) {
                    String arg = args[i + 1];

                    String[] contactAddr = arg.split(":");
                    if(contactAddr.length == 2) {
                        try {
                            contactNodeString = contactAddr[0];
                            if(contactNodeString.isEmpty()) {
                                throw new IllegalArgumentException();
                            }

                            contactNodePort = Integer.parseInt(contactAddr[1]);
                            if(contactNodePort < 0) {
                                throw new NumberFormatException();
                            }
                        } catch(NumberFormatException e) {
                            System.err.println("ERROR: Invalid contact node port specified");
                            throw new IllegalArgumentException();
                        } catch(IllegalArgumentException e) {
                            System.err.println("ERROR: Invalid contact node format specified. Expects <host>:<port> format.");
                            throw new IllegalArgumentException();
                        }
                    } else {
                        System.err.println("ERROR: Invalid contact node format specified. Expects <host>:<port> format.");
                        throw new IllegalArgumentException();
                    }

                }
                // Unrecognized flag
                else {
                    throw new IllegalArgumentException();
                }
            }
        }


    }


    /**
     * Prints usage message and exits the program
     */
    private static void printUsageMessageAndExit() {
        System.err.println("usage: [-c contact_node:port] [-p local_receiving_port] [-n node_ID]");
        System.exit(-1);
    }


}
