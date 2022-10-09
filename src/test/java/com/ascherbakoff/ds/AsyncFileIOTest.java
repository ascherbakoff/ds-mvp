package com.ascherbakoff.ds;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

public class AsyncFileIOTest {
    @Test
    public void testWriteRead() throws IOException {
        File file = File.createTempFile("ds-test-data-", ".tmp");
        file.deleteOnExit();

        AsyncFileIO fileIO = new AsyncFileIO(file, CREATE, READ, WRITE);

        int size = 1024;
        ByteBuffer buf = ByteBuffer.allocate(size);
        assertEquals(size, buf.capacity());
        TestUtils.fill(buf, (byte) 'a');
        assertTrue(TestUtils.match(buf, (byte) 'a'));

        fileIO.writeFully(buf, 0).join();

        ByteBuffer buf2 = ByteBuffer.allocate(size);
        fileIO.readFully(buf2, 0).join();

        assertTrue(TestUtils.match(buf2, (byte) 'a'));
    }
}
