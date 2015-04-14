package utility;

import nodelist.Node;

import java.io.*;
import java.util.ArrayList;

public class NodeSerializerUtility {

    /**
     * Deserializes a byte array containing a serialized array list of Node objects.
     * In other words, converts a series of bytes representing a array list of Node objects into an object
     * representation - ArrayList of Node objects
     *
     * ASSUMPTION: The byte array being passed is in fact a serialized array list of
     * Node objects
     *
     * @param bytes - Serialized array list of Node objects
     * @return - Array List of Node objects if successful, null if not.
     */
    public static ArrayList<Node> deserialize(byte[] bytes) {

        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
        ObjectInput in = null;

        ArrayList<Node> nodes = null;

        try {
            in = new ObjectInputStream(byteInputStream);

            nodes = (ArrayList<Node>) in.readObject();
            in.close();

        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(in != null) {
                    in.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return nodes;
    }


    /**
     * Serializes an array list of Node objects into a byte array
     *
     * @param nodeList - Node list to serialize
     * @return - Byte array of the serialized array list if successful, null if not.
     */
    public static byte[] serializeNodeList(ArrayList<Node> nodeList) {

        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        ObjectOutput out = null;

        byte[] bytes = null;

        try {
            out = new ObjectOutputStream(byteOutStream);
            out.writeObject(nodeList);

            bytes = byteOutStream.toByteArray();

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return bytes;
    }

}
