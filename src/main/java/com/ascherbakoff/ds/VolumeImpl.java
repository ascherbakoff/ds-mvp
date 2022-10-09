package com.ascherbakoff.ds;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class VolumeImpl implements Volume {
    private final int id;
    private final int size;
    private final DataExtentCache dataCache;
    private final int extSize;

    public VolumeImpl(int id, int size, int extSize, int blkSize, AsyncFileIO dataIO, Mapper mapper, Allocator allocator) {
        this.id = id;
        this.size = size;
        this.extSize = extSize;
        this.dataCache = new DataExtentCache(id, extSize, blkSize, dataIO, mapper, allocator);
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public CompletableFuture<Void> read(int lba, ByteBuffer buf) {
        return dataCache.read(lba, buf);
    }

    @Override
    public CompletableFuture<Void> write(int lba, ByteBuffer buf) {
        dataCache.write(lba, buf);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> unmap(int lba) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
        dataCache.flush();
    }

    @Override
    public ByteBuffer allocateBlock() {
        return ByteBuffer.allocate(dataCache.getBlockSize());
    }
}
