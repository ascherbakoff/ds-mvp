package com.ascherbakoff.ds;

import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

public class Mapper {
    private final AsyncFileIO metadataIO;
    private final int blocksInExtent;
    private ConcurrentHashMap<MapKey, Long> map = new ConcurrentHashMap<>();

    private int blockSize;
    private int extSize;

    public Mapper(int extSize, int blockSize, AsyncFileIO metadataIO) {
        this.extSize = extSize;
        this.blockSize = blockSize;
        this.blocksInExtent = extSize / blockSize;
        this.metadataIO = metadataIO;
    }

    /**
     * Maps a logical volume block to physical location of contatining extent.
     *
     * @param volumeId Volume id.
     * @param lba LBA lba.
     * @return
     */
    public @Nullable Long get(int volumeId, int lba) {
        int extLba = toExtentLba(lba);

        int idx = lba - extLba * blocksInExtent;

        Long realIdx = map.get(new MapKey(volumeId, extLba));

        if (realIdx == null)
            return null; // Not allocated

        return realIdx * blocksInExtent + idx;
    }

    public void put(int volumeId, int lba, long realIdx) {
        int extLba = toExtentLba(lba);

        map.putIfAbsent(new MapKey(volumeId, extLba), realIdx);
    }

    private int toExtentLba(int lba) {
        long tmp = lba * blockSize;

        return (int) (tmp / extSize);
    }

    private static class MapKey {
        private final int volumeId;
        private final int extLba;

        MapKey(int volumeId, int extLba) {
            this.volumeId = volumeId;
            this.extLba = extLba;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MapKey mapKey = (MapKey) o;

            if (volumeId != mapKey.volumeId) {
                return false;
            }
            if (extLba != mapKey.extLba) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = volumeId;
            result = 31 * result + extLba;
            return result;
        }
    }
}
