/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ascherbakoff.ds;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File I/O implementation based on {@link AsynchronousFileChannel}.
 */
public class AsyncFileIO extends AbstractFileIO {
    /**
     * File channel associated with {@code file}
     */
    private final AsynchronousFileChannel ch;
    private final Path path;

    /** */
    private Set<ChannelOpFuture> asyncFuts = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Creates I/O implementation for specified {@code file}
     * @param file Random access file
     * @param modes Open modes.
     */
    public AsyncFileIO(File file, OpenOption... modes) throws IOException {
        path = file.toPath();
        ch = AsynchronousFileChannel.open(path, modes);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelOpFuture read(ByteBuffer destBuf, long position) {
        ChannelOpFuture fut = new ChannelOpFuture();

        ch.read(destBuf, position, null, fut);

        return fut;
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<Integer> write(ByteBuffer srcBuf, long position) {
        ChannelOpFuture fut = new ChannelOpFuture();

        asyncFuts.add(fut);

        ch.write(srcBuf, position, null, fut);

        return fut.whenComplete((res, err) -> {
            asyncFuts.remove(fut);
        });
    }

    @Override
    public CompletableFuture<Void> readFully(ByteBuffer destBuf, long position) {
        return read(destBuf, position).thenComposeAsync(res -> {
            if (destBuf.remaining() > 0) {
                return readFully(destBuf, position + res);
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public CompletableFuture<Void> writeFully(ByteBuffer srcBuf, long position) {
        return write(srcBuf, position).thenComposeAsync(res -> {
            if (srcBuf.remaining() > 0) {
                return writeFully(srcBuf, position + res);
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void force() throws IOException {
        force(false);
    }

    /** {@inheritDoc} */
    @Override
    public void force(boolean withMetadata) throws IOException {
        ch.force(withMetadata);
    }

    /** {@inheritDoc} */
    @Override
    public long size() throws IOException {
        return ch.size();
    }

    /** {@inheritDoc} */
    @Override
    public void clear() throws IOException {
        ch.truncate(0);
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        for (ChannelOpFuture asyncFut : asyncFuts) {
            try {
                asyncFut.join(); // Ignore interrupts while waiting for channel close.
            }
            catch (Exception e) {
                throw new IOException(e);
            }
        }

        ch.close();
    }

    /** */
    static class ChannelOpFuture extends CompletableFuture<Integer> implements CompletionHandler<Integer, Void> {
        /** {@inheritDoc} */
        @Override
        public void completed(Integer res, Void attach) {
            // Release waiter and allow next operation to begin.
            super.complete(res);
        }

        /** {@inheritDoc} */
        @Override
        public void failed(Throwable exc, Void attach) {
            super.completeExceptionally(exc);
        }
    }

    @Override
    public String toString() {
        return path.toString();
    }
}