package com.ascherbakoff.ds;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DataExtentCache {
    public final int extSize;
    public final int blockSize;
    public final int blocksInExtent;
    private final AsyncFileIO dataIO;
    private final Mapper mapper;
    private final int volumeId;
    private final Allocator allocator;

    private ConcurrentHashMap<Integer, DataExtent> map = new ConcurrentHashMap<>();

    public DataExtentCache(int volumeId, int extSize, int blockSize, AsyncFileIO dataIO, Mapper mapper, Allocator allocator) {
        this.volumeId = volumeId;
        this.extSize = extSize;
        this.blockSize = blockSize;
        this.blocksInExtent = extSize / blockSize;
        this.dataIO = dataIO;
        this.mapper = mapper;
        this.allocator = allocator;
    }
    private int toExtentLba(int lba) {
        long tmp = lba * blockSize;

        return (int) (tmp / extSize);
    }

    public int getExtSize() {
        return extSize;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public CompletableFuture<Void> read(int lba, ByteBuffer dst) {
        int extLba = toExtentLba(lba);

        DataExtent extent = map.get(extLba);

        int blockIdx = lba - extLba * blocksInExtent;

        if (extent != null && extent.data[blockIdx] != null) {
            extent.readTo(blockIdx, dst);

            return CompletableFuture.completedFuture(null);
        }

        Long physIdx = mapper.get(volumeId, extLba);

        if (physIdx == null) {
            return CompletableFuture.failedFuture(new StorageException("No data for volumeId=" + volumeId + ", lba=" + lba));
        }

        long off = physIdx * extSize;

        if (extent == null) {
            extent = new DataExtent(blockSize, blocksInExtent);

            map.put(extLba, extent);
        }

        extent.data[blockIdx] = ByteBuffer.allocate(blockSize);

        DataExtent finalExtent = extent;
        return dataIO.readFully(extent.data[blockIdx], off).thenApply(ignored -> {
            finalExtent.readTo(blockIdx, dst);
            return null;
        });
    }

    void write(int lba, ByteBuffer src) {
        int extLba = toExtentLba(lba);

        DataExtent extent = map.get(extLba);

        if (extent == null) {
            extent = new DataExtent(blockSize, blocksInExtent);
        }

        map.putIfAbsent(extLba, extent);

        extent.writeFrom(lba - extLba * blocksInExtent, src);
    }

    CompletableFuture<Void> flush(Entry<Integer, DataExtent> entry) {
        int extLba = entry.getKey();
        DataExtent extent = entry.getValue();

        if (extent == null)
            return CompletableFuture.completedFuture(null);

        Long physIdx = mapper.get(volumeId, extLba);

        if (physIdx == null) {
            physIdx = allocator.allocate();
            mapper.put(volumeId, extLba, physIdx);
        }

        long physOff = physIdx * extSize;

        CompletableFuture fut = CompletableFuture.completedFuture(null);

        for (int i = 0; i < extent.data.length; i++) {
            ByteBuffer buf = extent.data[i];

            if (buf != null) {
                int finalI = i;
                fut = fut.thenComposeAsync(ignored -> dataIO.writeFully(buf, physOff + finalI * blockSize));
            }
        }

        return fut;
    }

    public void flush() {
        Set<Entry<Integer, DataExtent>> set = map.entrySet();

        Iterator<Entry<Integer, DataExtent>> iterator = set.iterator();

        while (iterator.hasNext()) {
            Entry<Integer, DataExtent> next = iterator.next();
            flush(next).join();
            iterator.remove();
        }
    }
}
