package com.ascherbakoff.ds;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface Volume {
    int id();

    /**
     * @param volumeId A volume id.
     * @param lba A logical block address
     * @param buf A buffer to fill
     */
    CompletableFuture<Void> read(int lba, ByteBuffer buf);

    CompletableFuture<Void> write(int lba, ByteBuffer buf);

    CompletableFuture<Void> unmap(int lba);

    ByteBuffer allocateBlock();

    void flush();
}
