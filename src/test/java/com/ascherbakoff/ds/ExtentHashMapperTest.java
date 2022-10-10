package com.ascherbakoff.ds;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtentHashMapperTest {
    AsyncFileIO metadataIO;
    int blkSize;
    int extSize;

    private final ExtentHashMapper mapper;

    private final int blocksInExtent;

    {
        File metadataFile;
        try {
            metadataFile = File.createTempFile("ds-test-metadata-", ".tmp");
            metadataFile.deleteOnExit();

            metadataIO = new AsyncFileIO(metadataFile, CREATE, READ, WRITE);
            blkSize = 4;
            extSize = 32;
            blocksInExtent = extSize / blkSize;

            mapper = new ExtentHashMapper(extSize, blkSize, metadataIO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMapper() {
        // Not every mapping is correct. Blocks must be mapped to the corresponding positions in extents.
        mapper.put(0, 0);
        Assertions.assertEquals(0, mapper.get(0));

        mapper.put(1, 1);
        Assertions.assertEquals(1, mapper.get(1));

        mapper.put(blocksInExtent, blocksInExtent * 10);
        Assertions.assertEquals(blocksInExtent * 10, mapper.get(blocksInExtent));

        mapper.put(blocksInExtent * 2 + 2, blocksInExtent * 8 + 2);
        Assertions.assertEquals(blocksInExtent * 8 + 2, mapper.get(blocksInExtent * 2 + 2));
    }
}
