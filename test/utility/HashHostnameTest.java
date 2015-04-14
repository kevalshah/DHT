package utility;


import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class HashHostnameTest {


    @Test
    public void testDuplicateHashTestWithActualHosts() throws Exception {
        ArrayList<Integer> hostNameHashes = new ArrayList<Integer>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("test/utility/Hostnames.txt"));
            String host = null;
            int i = 1;
            while ((host = br.readLine()) != null)
            {
                Integer hash = new Integer(HashUtility.hashString(host));
                System.out.println(i + ") " + host + " : " + hash);
                if(hostNameHashes.contains(hash)) {
                    fail("Duplicate hash detected = " + hash);
                } else {
                    hostNameHashes.add(hash);
                }
                i++;
            }
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            if(br != null) {
                br.close();
            }
        }


    }
}
