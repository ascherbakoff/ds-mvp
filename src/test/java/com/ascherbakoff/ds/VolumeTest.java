package com.ascherbakoff.ds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;

public class VolumeTest extends AbstractVolumeTest {
    @Test
    public void testCreateVolume() {
        Volume vol = mgr.create(1024 * 1024);
        assertEquals(1, vol.id());
    }

    @Test
    public void testCacheWriteRead() {
        Volume vol = mgr.create(1024 * 1024);

        byte a = (byte) 'a';
        for (int i = 0; i < 25; i++) {
            ByteBuffer buf = mgr.allocateBlock();
            TestUtils.fill(buf, (byte) (a + i));
            vol.write(i, buf);
        }

        for (int i = 0; i < 25; i++) {
            ByteBuffer buf = mgr.allocateBlock();
            vol.read(i, buf);
            assertTrue(TestUtils.match(buf, (byte) (a + i)));
        }
    }

    @Test
    public void testNotFound() {
        Volume vol = mgr.create(1024 * 1024);

        ByteBuffer buf = mgr.allocateBlock();
        var err = assertThrows(CompletionException.class, () -> vol.read(0, buf).join());
        assertEquals(StorageException.class, err.getCause().getClass());
    }

    @Test
    public void testFlushSingle() throws IOException {
        Volume vol1 = mgr.create(1024 * 1024);

        ByteBuffer buf1 = mgr.allocateBlock();
        TestUtils.fill(buf1, (byte) 'a');
        vol1.write(0, buf1);

        TestUtils.fill(buf1, (byte) 0);

        vol1.read(0, buf1);

        assertTrue(TestUtils.match(buf1, (byte) 'a'));

        assertEquals(0, mgr.getAllocator().allocated());

        vol1.flush();

        assertEquals(1, mgr.getAllocator().allocated());

        assertEquals(blkSize, dataIO.size());

        TestUtils.fill(buf1, (byte) 0);
        vol1.read(0, buf1).join();
        assertTrue(TestUtils.match(buf1, (byte) 'a'));
    }

    @Test
    public void testFlushMany() throws IOException {
        Volume vol1 = mgr.create(1024 * 1024);
        Volume vol2 = mgr.create(1024 * 1024);

        ByteBuffer buf1 = mgr.allocateBlock();
        TestUtils.fill(buf1, (byte) 'a');
        vol1.write(0, buf1);

        ByteBuffer buf2 = mgr.allocateBlock();
        TestUtils.fill(buf2, (byte) 'b');
        vol2.write(0, buf2);

        TestUtils.fill(buf1, (byte) 0);
        TestUtils.fill(buf2, (byte) 0);

        vol1.read(0, buf1).join();
        vol2.read(0, buf2).join();

        assertTrue(TestUtils.match(buf1, (byte) 'a'));
        assertTrue(TestUtils.match(buf2, (byte) 'b'));

        assertEquals(0, mgr.getAllocator().allocated());
        vol1.flush();

        assertEquals(1, mgr.getAllocator().allocated());
        vol2.flush();

        assertEquals(2, mgr.getAllocator().allocated());

        assertEquals(extSize + blkSize, dataIO.size());

        TestUtils.fill(buf1, (byte) 0);
        TestUtils.fill(buf2, (byte) 0);

        vol1.read(0, buf1).join();
        vol2.read(0, buf2).join();

        assertTrue(TestUtils.match(buf1, (byte) 'a'));
        assertTrue(TestUtils.match(buf2, (byte) 'b'));
    }
}
