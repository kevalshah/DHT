package kvstore;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class KVStoreControllerTest {


    @Test
    public void testIsKVStoreFullForEmptyStore() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        assertEquals(false, kvStoreController.isKVStoreFull());
    }


    @Test
    public void testGetMaxCapacity() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(5);
        assertEquals(5, kvStoreController.getMaxCapacity());
    }


    @Test
    public void testPutOnEmptyStore() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        try {
            kvStoreController.put("key", "value");
        } catch(Exception e) {
            fail();
        }
    }


    @Test(expected = KVStoreInvalidKeyOrValueFormatException.class)
    public void testPutWithNullKey() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        kvStoreController.put(null, "value");
    }


    @Test(expected = KVStoreInvalidKeyOrValueFormatException.class)
    public void testPutWithNullValue() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        kvStoreController.put("key", null);
    }


    @Test(expected = KVStoreInvalidKeyOrValueFormatException.class)
    public void testPutWithEmptyKeyValue() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        kvStoreController.put("", "value");
    }


    @Test(expected = KVStoreInvalidKeyOrValueFormatException.class)
    public void testPutWithEmptyValue() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        kvStoreController.put("key", "");
    }


    @Test(expected = KVStoreFullException.class)
    public void testPutOnFullStore() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(1);
        kvStoreController.put("key", "value");
        kvStoreController.put("key", "value");
    }

    @Test
    public void testPutWithExistingKey() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        kvStoreController.put("key", "value");
        kvStoreController.put("key", "value2");
        assertEquals("value2", kvStoreController.get("key"));
    }

    @Test
    public void testGet() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        kvStoreController.put("key", "value");
        try {
            assertEquals("value", kvStoreController.get("key"));
        } catch(Exception e) {
            fail();
        }
    }


    @Test(expected = KVStoreKeyNotFoundException.class)
    public void testGetWithNonExistingKey() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        kvStoreController.put("key", "value");
        kvStoreController.get("non-existing-key");
    }


    @Test
    public void testRemove() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        kvStoreController.put("key", "value");
        kvStoreController.remove("key");
        try {
            kvStoreController.get("key");
            fail();
        } catch(KVStoreKeyNotFoundException e) {

        }
    }


    @Test(expected = KVStoreKeyNotFoundException.class)
    public void testRemoveOnEmptyStore() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        kvStoreController.remove("key");
    }


    @Test(expected = KVStoreKeyNotFoundException.class)
    public void testRemoveNonExistingKey() throws Exception {
        KVStoreController kvStoreController = new KVStoreController(2);
        kvStoreController.put("key", "value");
        kvStoreController.remove("non-existing-key");
    }


    @Test
    public void testSingletonGetInstance() {

        String key = "key";
        String value = "value";

        KVStoreController kvStoreController = KVStoreController.getInstance();
        try {
            kvStoreController.put(key, value);
            assertEquals(value, kvStoreController.get(key));

            KVStoreController kvStoreController1 = KVStoreController.getInstance();
            assertEquals(value, kvStoreController1.get(key));

        } catch(Exception e) {
            fail();
        }

    }


    @Test
    public void testSingletonGetInstanceWithMultipleThreads() {

        String key = "key";
        String value = "value";

        KVStoreController kvStoreController = KVStoreController.getInstance();
        try {
            kvStoreController.put(key, value);
            assertEquals(value, kvStoreController.get(key));

            new Thread( new Runnable() {
                @Override
                public void run() {
                    String key = "key";
                    String value = "value";
                    KVStoreController kvStoreController1 = KVStoreController.getInstance();
                    try {
                        assertEquals(value, kvStoreController1.get(key));
                    } catch(KVStoreKeyNotFoundException e) {
                        fail();
                    }
                }
            }).start();

            new Thread( new Runnable() {
                @Override
                public void run() {
                    String key = "key";
                    String value = "value";
                    KVStoreController kvStoreController1 = KVStoreController.getInstance();
                    try {
                        assertEquals(value, kvStoreController1.get(key));
                    } catch(KVStoreKeyNotFoundException e) {
                        fail();
                    }
                }
            }).start();

        } catch(Exception e) {
            fail();
        }

    }


}