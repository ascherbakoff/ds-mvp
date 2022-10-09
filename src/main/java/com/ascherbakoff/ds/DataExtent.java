package com.ascherbakoff.ds;

import java.nio.ByteBuffer;

public class DataExtent {
    final ByteBuffer[] data;
    private final int length;

    public DataExtent(int lenth, int cnt) {
        this.length = lenth;
        this.data = new ByteBuffer[cnt]; // Memory is allocated lazily
    }

    public void writeFrom(int idx, ByteBuffer buf) {
        if (data[idx] == null) {
            data[idx] = ByteBuffer.allocate(length);
        }

        data[idx].position(0);
        data[idx].put(buf);
        data[idx].flip();
        buf.flip();
    }

    public void readTo(int idx, ByteBuffer buf) {
        assert data[idx] != null;

        data[idx].position(0);
        buf.put(data[idx]);
        buf.flip();
        data[idx].flip();
    }
}
