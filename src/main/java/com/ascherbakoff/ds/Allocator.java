package com.ascherbakoff.ds;

import java.io.IOException;

public class Allocator {
    private final AsyncFileIO fileIO;
    private final int chunkSize;

    int allocated;

    public Allocator(int chunkSize, AsyncFileIO fileIO) throws IOException {
        this.chunkSize = chunkSize;
        this.fileIO = fileIO;
        this.allocated = (int) (fileIO.size() / chunkSize);
    }

    long allocate() {
        return allocated++;
    }

    long allocated() {
        return allocated;
    }

    void release(long idx) {
        // TODO
    }
}
