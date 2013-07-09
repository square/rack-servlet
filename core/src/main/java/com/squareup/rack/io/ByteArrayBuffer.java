package com.squareup.rack.io;

import java.io.ByteArrayOutputStream;

public class ByteArrayBuffer extends ByteArrayOutputStream {
  public ByteArrayBuffer() {
  }

  public ByteArrayBuffer(int initialSize) {
    super(initialSize);
  }

  public byte[] getBuffer() {
    return buf;
  }

  public int getLength() {
    return count;
  }

  public void setLength(int length) {
    count = length;
  }
}
