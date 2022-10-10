package com.ascherbakoff.ds;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

public class IndirectTreeMapper implements Mapper {
    private final AsyncFileIO metadataIO;
    private final int size;
    private final Allocator metaAllocator;
    private final int nodeSize;

    private ConcurrentHashMap<Integer, ByteBuffer> cache = new ConcurrentHashMap<>();

    private Node root;

    public IndirectTreeMapper(int size, AsyncFileIO metadataIO, Allocator metaAllocator) throws IOException {
        this.size = size;
        this.metadataIO = metadataIO;
        this.metaAllocator = metaAllocator;

        nodeSize = size * 8;

        // Preload root. TODO support multiple volumes
        if (metadataIO.size() >= nodeSize) {
            ByteBuffer tmp = ByteBuffer.allocate(nodeSize);
            int rootLba = 0;
            metadataIO.read(tmp, rootLba).join();
            cache.put(0, tmp);
            root = new Node(tmp.asLongBuffer());
        }
        else {
            long allocated = metaAllocator.allocate();
            ByteBuffer refs = ByteBuffer.allocate(nodeSize);
            metadataIO.writeFully(refs, allocated * nodeSize).join();
            refs.flip();
            root = new Node(refs.asLongBuffer()); // All zeroes
        }
    }

    @Override
    public @Nullable Long get(long lba) {
        int topIdx = 0;
        int midIdx = 0;
        int leafIdx = 0;

        return null;
    }

    @Override
    public void put(long lba, long realIdx) {
        int topIdx = 0;
        int midIdx = 0;
        int leafIdx = 0;
    }

    private static class Node {
        Node[] children;
        final LongBuffer refs;

        Node(LongBuffer refs) {
            this.refs = refs;
            children = new Node[refs.limit()];
        }
    }
}
