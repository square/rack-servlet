package com.squareup.rack;

import com.google.common.io.ByteStreams;
import com.squareup.rack.io.ByteArrayBuffer;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts an InputStream to the required interface for rack.input.
 *
 * Speaks byte[], not String, because Java doesn't support the equivalent of Ruby strings with ASCII-8BIT encoding.
 *
 * We later further adapt this class to speak Ruby inside JRubyRackApplication.
 */
public class RackInput implements Closeable {
  private static final int READ_AHEAD_SUGGESTION = 1024 * 1024;
  private static final int LINEFEED = 0xA;

  private final InputStream stream;
  private final ByteArrayBuffer buffer = new ByteArrayBuffer();
  private int bufferReadHead;

  public RackInput(InputStream inputStream) throws IOException {
    checkNotNull(inputStream);
    checkArgument(inputStream.markSupported(), "rack.input must be rewindable, but inputStream doesn't support mark.");
    stream = inputStream;
    stream.mark(READ_AHEAD_SUGGESTION);
  }

  /**
   * Override this if you want to allow for really really really long lines to be returned by
   * {@link com.squareup.rack.RackInput#gets()}.
   */
  protected int getMaxLineLength() {
    return READ_AHEAD_SUGGESTION;
  }

  public byte[] gets() throws IOException {
    return readToLinefeed();
  }

  public byte[] read(@Nullable Integer length) throws IOException {
    if (length == null) {
      return readToEof();
    } else {
      return readTo(length);
    }
  }

  public void rewind() throws IOException {
    stream.reset();
    buffer.reset();
    bufferReadHead = 0;
  }

  private byte[] readToLinefeed() throws IOException {
    int startFrom = 0;
    do {
      int indexOfNewline = indexOfNextNewlineInBuffer(startFrom);

      if (indexOfNewline == -1) {
        int bytesPresent = bytesAvailableInBuffer();

        if (bytesPresent > getMaxLineLength()) {
          throw new RuntimeException(
              "Really, you have a line longer than " + getMaxLineLength() + " bytes?");
        }

        // next time through, start where we left off.
        startFrom = bytesPresent;

        int bytesRead = fillBuffer(8 * 1024);
        if (bytesRead == -1) {
          int bytesRemaining = bytesAvailableInBuffer();
          return consumeBytesFromBuffer(bytesRemaining);
        }
      } else {
        int length = indexOfNewline - bufferReadHead + 1;
        return consumeBytesFromBuffer(length);
      }
    } while (true);
  }

  private byte[] readToEof() throws IOException {
    if (bufferReadHead > 0) {
      compactBuffer(true);
    }

    ByteStreams.copy(stream, buffer);

    int length = buffer.getLength();
    if (length == 0) {
      return new byte[0];
    } else {
      return consumeBytesFromBuffer(length);
    }
  }

  private byte[] readTo(int length) throws IOException {
    if (length == 0) {
      return new byte[0];
    }

    if (bufferReadHead > 0) {
      compactBuffer(true);
    }

    int bytesStillNeeded = length - buffer.getLength();
    if (bytesStillNeeded > 0) {
      fillBuffer(bytesStillNeeded);
    }

    return consumeBytesFromBuffer(length);
  }

  private int bytesAvailableInBuffer() {
    return buffer.getLength() - bufferReadHead;
  }

  private int indexOfNextNewlineInBuffer(int startFrom) {
    byte[] bytes = buffer.getBuffer();
    int bufferLength = buffer.getLength();
    for (int i = bufferReadHead + startFrom; i < bufferLength; i++) {
      if (bytes[i] == LINEFEED) {
        return i;
      }
    }
    return -1;
  }

  private int fillBuffer(int length) throws IOException {
    compactBuffer(false);
    byte[] readBuf = new byte[length];
    int bytesRead = stream.read(readBuf);
    if (bytesRead > 0) {
      buffer.write(readBuf, 0, bytesRead);
    }
    return bytesRead;
  }

  private byte[] consumeBytesFromBuffer(int length) {
    int bytesAvailable = bytesAvailableInBuffer();
    if (length > bytesAvailable) {
      length = bytesAvailable;
    }

    if (length == 0) {
      return null;
    }

    byte[] bytes = new byte[length];
    byte[] bufferBytes = buffer.getBuffer();
    System.arraycopy(bufferBytes, bufferReadHead, bytes, 0, length);
    bufferReadHead += length;
    return bytes;
  }

  private void compactBuffer(boolean force) {
    byte[] bufferBytes = buffer.getBuffer();

    // normally, only compact if we're at least 1K in, and at least 3/4 of the way in
    if (force || (bufferReadHead > 1024 && bufferReadHead > (bufferBytes.length * 3 / 4))) {
      int remainingBytes = bytesAvailableInBuffer();
      System.arraycopy(bufferBytes, bufferReadHead, bufferBytes, 0, remainingBytes);
      buffer.setLength(remainingBytes);
      bufferReadHead = 0;
    }
  }

  @Override public void close() throws IOException {
    stream.close();
  }
}
