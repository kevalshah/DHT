package message;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static org.junit.Assert.*;

public class HeaderTest {

    @Test
    public void testBuildUniqueIDSize() {
        assertEquals(Header.UNIQUE_ID_SIZE_BYTES, Header.buildUniqueID((short)0).length);
    }

    @Test
    public void testBuildUniqueID() throws UnknownHostException {

        byte[] localAddress = InetAddress.getLocalHost().getAddress();
        short port = 14;

        byte[] uniqueID = Header.buildUniqueID(port);

        assertArrayEquals(localAddress, Arrays.copyOfRange(uniqueID, 0, 4));

        byte[] portBytes = Arrays.copyOfRange(uniqueID, 4, 6);
        short actualPort = ByteBuffer.wrap(portBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        assertEquals(port, actualPort);
    }




}