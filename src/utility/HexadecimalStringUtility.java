package utility;


public class HexadecimalStringUtility {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Converts a byte array into a hexadecimal string representation
     *
     * Code snippet from: http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
     *
     * @param bytes - Bytes to convert
     * @return - Hexadecimal string representation of input byte array
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }



}
