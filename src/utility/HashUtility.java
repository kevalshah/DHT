package utility;

public class HashUtility {

    /**
     * Hashes a string to a positive integer
     * @param stringToHash - String to hash
     * @throws java.lang.IllegalArgumentException if input string is null or empty
     * @return - Integer hash value of input string
     */
    public static int hashString(String stringToHash) throws IllegalArgumentException {
        if(stringToHash == null || stringToHash.isEmpty()) {
            throw new IllegalArgumentException();
        }

        int hash = 7;
        for (int i = 0; i < stringToHash.length(); i++) {
            hash = hash * 31 + stringToHash.charAt(i);
        }

        if(hash < 0) {
            hash = Math.abs(hash);
        }

        return hash;
    }


    /**
     * Simple hash function that sums the ASCII values of the letters in a string
     * @param string - String to hash
     * @param range - Range of hash value to produce
     * @return - Hash value of input string in specified range
     * @throws IllegalArgumentException - if input string is null or empty, or range <= 0
     */
    public static int simpleHash(String string, int range) throws IllegalArgumentException {

        if(string == null || string.isEmpty() || range <= 0) {
            throw new IllegalArgumentException();
        }

        int i, sum;
        for (sum = 0, i = 0; i < string.length(); i++)
            sum += string.charAt(i);

        return Math.abs(sum) % range;
    }

}

