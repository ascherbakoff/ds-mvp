package com.ascherbakoff.ds;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;
import java.io.IOException;

public class AbstractVolumeTest {
    VolumeManager mgr;
    AsyncFileIO dataIO;
    AsyncFileIO metadataIO;
    int blkSize;
    int extSize;

    {
        File dataFile = null;
        File metadataFile = null;
        try {
            dataFile = File.createTempFile("ds-test-data-", ".tmp");
            dataFile.deleteOnExit();

            metadataFile = File.createTempFile("ds-test-metadata-", ".tmp");
            metadataFile.deleteOnExit();

            dataIO = new AsyncFileIO(dataFile, CREATE, READ, WRITE);
            metadataIO = new AsyncFileIO(metadataFile, CREATE, READ, WRITE);
            blkSize = 4;
            extSize = 32;
            mgr = new VolumeManager(extSize, blkSize, dataIO, metadataIO, new MapperFactory() {
                @Override
                public Mapper create() {
                    return new ExtentHashMapper(extSize, blkSize, metadataIO);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
