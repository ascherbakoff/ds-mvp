package com.ascherbakoff.ds;

import java.util.concurrent.ConcurrentHashMap;

public class Mapper {
    private final AsyncFileIO metadataIO;
    private ConcurrentHashMap<MapKey, Long> map = new ConcurrentHashMap<>();

    public Mapper(AsyncFileIO metadataIO) {
        this.metadataIO = metadataIO;
    }

    /**
     * Maps a logical volume block to physical location of contatining extent.
     *
     * @param volumeId Volume id.
     * @param lba LBA.
     * @return
     */
    public Long get(int volumeId, int extLba) {
        return map.get(new MapKey(volumeId, extLba));
    }

    public void put(int volumeId, int extLba, long physIdx) {
        map.put(new MapKey(volumeId, extLba), physIdx);
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
