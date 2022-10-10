package com.ascherbakoff.ds;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VolumeManager {
    private final Allocator allocator;
    private final MapperFactory factory;
    AtomicInteger idGen = new AtomicInteger();

    private Map<Integer, Volume> vols = new ConcurrentHashMap<>();

    private AsyncFileIO dataIO;

    private final int extSize;
    private final int blkSize;

    public VolumeManager(int extSize, int blkSize, AsyncFileIO dataIO, AsyncFileIO metadataIO, MapperFactory factory) throws IOException {
        this.extSize = extSize;
        this.blkSize = blkSize;
        this.dataIO = dataIO;
        this.factory = factory;
        this.allocator = new Allocator(extSize, dataIO);
    }

    /**
     * @param volSize Size in bytes.
     * @return A volume.
     */
    public Volume create(int volSize) {
        int volumeId = idGen.incrementAndGet();

        Mapper mapper = factory.create();

        VolumeImpl vol = new VolumeImpl(volumeId, volSize, new DataExtentCache(extSize, blkSize, dataIO, mapper, allocator));

        vols.put(volumeId, vol);

        return vol;
    }

    public Allocator getAllocator() {
        return allocator;
    }

    public ByteBuffer allocateBlock() {
        return ByteBuffer.allocate(blkSize);
    }
}
