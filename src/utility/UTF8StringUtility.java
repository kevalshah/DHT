package utility;

import java.io.UnsupportedEncodingException;

public class UTF8StringUtility {

    /**
     * Converts bytes encoded in UTF-8 format to a string
     * @param bytesUTF8Encoding - Bytes in UTF-8 format to convert
     * @return - String representation of bytes
     */
    public static String bytesUTF8ToString(byte[] bytesUTF8Encoding) {

        String s = null;

        try {
            s = new String(bytesUTF8Encoding, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return s;
    }



    /**
     * Converts string to bytes encoded in UTF-8 format
     * @param string - String to convert
     * @return - Bytes in UTF-8 format
     */
    public static byte[] stringToBytesUTF8(String string) {

        byte[] bytes = null;

        try {
            bytes = string.getBytes("UTF-8");
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return bytes;
    }




}
