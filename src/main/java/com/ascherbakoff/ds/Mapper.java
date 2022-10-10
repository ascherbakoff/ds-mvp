package com.ascherbakoff.ds;

import org.jetbrains.annotations.Nullable;

public interface Mapper {
    @Nullable Long get(long lba);

    void put(long lba, long realIdx);
}
