package com.ascherbakoff.ds;

import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

public class ExtentHashMapper implements Mapper {
    private final AsyncFileIO metadataIO;
    private final int blocksInExtent;
    private ConcurrentHashMap<Long, Long> map = new ConcurrentHashMap<>();

    private int blockSize;
    private int extSize;

    public ExtentHashMapper(int extSize, int blockSize, AsyncFileIO metadataIO) {
        this.extSize = extSize;
        this.blockSize = blockSize;
        this.blocksInExtent = extSize / blockSize;
        this.metadataIO = metadataIO;
    }

    /**
     * Maps a logical volume block to physical location of contatining extent.
     *
     * @param lba LBA lba.
     */
    @Override
    public @Nullable Long get(long lba) {
        long extLba = toExtentLba(lba);

        long off = lba - extLba * blocksInExtent;

        Long realIdx = map.get(extLba);

        if (realIdx == null)
            return null; // Not allocated

        return realIdx * blocksInExtent + off;
    }

    @Override
    public void put(long lba, long realIdx) {
        long extLba = toExtentLba(lba);
        long real = toExtentLba(realIdx);

        long off = lba - extLba * blocksInExtent;
        long off2 = realIdx - real * blocksInExtent;

        if (off != off2)
            throw new IllegalArgumentException("Invalid mapping");

        map.putIfAbsent(extLba, real);
    }

    private long toExtentLba(long lba) {
        long tmp = lba * blockSize;

        return (int) (tmp / extSize);
    }
}
