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

import com.ascherbakoff.ds.AsyncFileIO.ChannelOpFuture;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Interface to perform file I/O operations.
 */
public interface FileIO extends AutoCloseable {
    /**
     * Reads a sequence of bytes from this file into the {@code destinationBuffer}
     * starting from specified file {@code position}.
     *
     * @param destBuf Destination byte buffer.
     * @param position Starting position of file.
     *
     * @return Number of read bytes, possibly zero, or <tt>-1</tt> if the
     *          given position is greater than or equal to the file's current
     *          size
     *
     * @throws IOException If some I/O error occurs.
     */
    public ChannelOpFuture read(ByteBuffer destBuf, long position) throws IOException;

    /**
     * Reads a sequence of bytes from this file into the {@code destinationBuffer}
     * starting from specified file {@code position}.
     *
     * @param destBuf Destination byte buffer.
     * @param position Starting position of file.
     *
     * @return Number of written bytes.
     *
     * @throws IOException If some I/O error occurs.
     */
    public CompletableFuture<Void> readFully(ByteBuffer destBuf, long position) throws IOException;

    /**
     * Writes a sequence of bytes to this file from the {@code sourceBuffer}
     * starting from specified file {@code position}
     *
     * @param srcBuf Source buffer.
     * @param position Starting file position.
     *
     * @return Number of written bytes.
     *
     * @throws IOException If some I/O error occurs.
     */
    public CompletableFuture<Integer> write(ByteBuffer srcBuf, long position) throws IOException;

    /**
     * Writes a sequence of bytes to this file from the {@code sourceBuffer}
     * starting from specified file {@code position}
     *
     * @param srcBuf Source buffer.
     * @param position Starting file position.
     *
     * @return Number of written bytes.
     *
     * @throws IOException If some I/O error occurs.
     */
    public CompletableFuture<Void> writeFully(ByteBuffer srcBuf, long position) throws IOException;

    /**
     * Forces any updates of this file to be written to the storage
     * device that contains it.
     *
     * @throws IOException If some I/O error occurs.
     */
    public void force() throws IOException;

    /**
     * Forces any updates of this file to be written to the storage
     * device that contains it.
     *
     * @param withMetadata If {@code true} force also file metadata.
     * @throws IOException If some I/O error occurs.
     */
    public void force(boolean withMetadata) throws IOException;

    /**
     * Returns current file size in bytes.
     *
     * @return File size.
     *
     * @throws IOException If some I/O error occurs.
     */
    public long size() throws IOException;

    /**
     * Truncates current file to zero length
     * and resets current file position to zero.
     *
     * @throws IOException If some I/O error occurs.
     */
    public void clear() throws IOException;

    /**
     * Closes current file.
     *
     * @throws IOException If some I/O error occurs.
     */
    @Override
    public void close() throws IOException;
}
