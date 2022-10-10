package com.ascherbakoff.ds;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class VolumeImpl implements Volume {
    private final int id;
    private final DataExtentCache dataCache;

    public VolumeImpl(int id, int size, DataExtentCache dataCache) {
        this.id = id;
        this.dataCache = dataCache;
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
