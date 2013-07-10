/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.rack.io;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * <p>Buffers an {@link InputStream}, making it effectively rewindable.</p>
 *
 * <p>Operates in-memory, just like a {@link java.io.BufferedInputStream}, up to a size threshold,
 * then begins buffering to disk once that size threshold is crossed.</p>
 *
 * <p>As compared with Guava's {@link com.google.common.io.FileBackedOutputStream}, does not require
 * processing the entire stream before offering its contents to client code.</p>
 *
 * <p>Uses the default temporary-file directory, which you can control by setting the
 * {@code java.io.tmpdir} system property.</p>
 *
 * @see File#createTempFile(String, String)
 */
public class TempfileBufferedInputStream extends InputStream {
  private static final int DEFAULT_THRESHOLD = 1024 * 1024;

  private final InputStream source;

  private long readHead;
  private long writeHead;
  private long markPos;
  private Buffer buffer;

  /**
   * Buffers a source InputStream, dropping to disk once a default size threshold has been crossed.
   *
   * @param source the InputStream to buffer.
   */
  public TempfileBufferedInputStream(InputStream source) {
    this(source, DEFAULT_THRESHOLD);
  }

  /**
   * Buffers a source InputStream, dropping to disk once the given size threshold has been crossed.
   *
   * @param source the InputStream to buffer.
   * @param threshold the size threshold beyond which to buffer to disk.
   */
  public TempfileBufferedInputStream(InputStream source, int threshold) {
    Preconditions.checkNotNull(source);
    this.source = source;
    this.buffer = new MemoryBuffer(threshold);
  }

  @Override public int read() throws IOException {
    byte[] bytes = new byte[1];
    int read = read(bytes);
    return (read > 0) ? bytes[0] & 0xff : -1;
  }

  @Override public int read(byte[] bytes) throws IOException {
    return read(bytes, 0, bytes.length);
  }

  @Override public int read(byte[] bytes, int offset, int length) throws IOException {
    int bytesRead;

    long cachedReadable = writeHead - readHead;
    if (cachedReadable > 0) {
      int bytesToTransfer = Math.min(length, (int) cachedReadable);
      buffer.replay(bytes, offset, bytesToTransfer);
      bytesRead = bytesToTransfer;
      readHead += bytesRead;
    } else {
      bytesRead = source.read(bytes, offset, length);
      if (bytesRead > 0) {
        if (buffer.wouldOverflow(writeHead + bytesRead)) {
          buffer = buffer.embiggened();
        }

        buffer.append(bytes, offset, bytesRead);
        writeHead += bytesRead;
        readHead += bytesRead;
      }
    }

    return bytesRead;
  }

  @Override public synchronized void reset() throws IOException {
    readHead = markPos;
    buffer.sync();
  }

  @Override public synchronized void mark(int i) {
    markPos = readHead;
  }

  @Override public boolean markSupported() {
    return true;
  }

  @Override public void close() throws IOException {
    try {
      buffer.close();
    } catch (IOException e) {
      // safe to ignore, we're just closing a buffer
    }
    source.close();
  }

  interface Buffer {
    void replay(byte[] bytes, int offset, int length) throws IOException;

    void append(byte[] bytes, int offset, int length) throws IOException;

    boolean wouldOverflow(long length);

    Buffer embiggened() throws IOException;

    void sync() throws IOException;

    void close() throws IOException;
  }

  class MemoryBuffer implements Buffer {
    private final ByteArrayBuffer cacheOutputStream;
    private final int threshold;

    public MemoryBuffer(int threshold) {
      this.threshold = threshold;
      this.cacheOutputStream = new ByteArrayBuffer(threshold);
    }

    public void replay(byte[] bytes, int offset, int bytesToTransfer) {
      byte[] cacheBytes = cacheOutputStream.getBuffer();
      // Cast is safe because threshold is an int. (Arrays can only have integer indexes.)
      System.arraycopy(cacheBytes, (int) readHead, bytes, offset, bytesToTransfer);
    }

    @Override public void append(byte[] bytes, int offset, int length) {
      cacheOutputStream.write(bytes, offset, length);
    }

    @Override public boolean wouldOverflow(long length) {
      return length > threshold;
    }

    @Override public Buffer embiggened() throws IOException {
      return new FileBackedBuffer(cacheOutputStream);
    }

    @Override public void sync() {
    }

    @Override public void close() {
    }
  }

  private class FileBackedBuffer implements Buffer {
    private final BufferedOutputStream outputStream;
    private final FileChannel inputChannel;
    private MappedByteBuffer mappedByteBuffer;

    public FileBackedBuffer(ByteArrayBuffer baos) throws IOException {
      File tempFile = File.createTempFile("stream-buffer", ".buf");
      try {
        FileOutputStream fileOutputStream = createFileOutputStream(tempFile);
        outputStream = new BufferedOutputStream(fileOutputStream);
        outputStream.write(baos.getBuffer(), 0, baos.getLength());

        inputChannel = createFileInputStream(tempFile).getChannel();
      } finally {
        //noinspection ResultOfMethodCallIgnored
        tempFile.delete();
      }
    }

    @Override public void replay(byte[] bytes, int offset, int length) throws IOException {
      if (mappedByteBuffer == null) {
        mappedByteBuffer = inputChannel.map(FileChannel.MapMode.READ_ONLY, 0, writeHead);
      }
      // This cast is only unsafe if writeHead is > MAX_INT, i.e., the file is > 2GB. Unlikely?
      mappedByteBuffer.position((int) readHead);
      mappedByteBuffer.get(bytes, offset, length);
    }

    @Override public void append(byte[] bytes, int offset, int length) throws IOException {
      outputStream.write(bytes, offset, length);
    }

    @Override public boolean wouldOverflow(long length) {
      return false;
    }

    @Override public Buffer embiggened() {
      throw new UnsupportedOperationException();
    }

    @Override public void sync() throws IOException {
      outputStream.flush();
      mappedByteBuffer = null;
    }

    @Override public void close() throws IOException {
      mappedByteBuffer = null;
      try {
        outputStream.close();
      } finally {
        inputChannel.close();
      }
    }
  }

  @VisibleForTesting FileInputStream createFileInputStream(File tempFile)
      throws FileNotFoundException {
    return new FileInputStream(tempFile);
  }

  @VisibleForTesting FileOutputStream createFileOutputStream(File tempFile)
      throws FileNotFoundException {
    return new FileOutputStream(tempFile);
  }
}
