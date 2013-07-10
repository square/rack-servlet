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

import java.io.ByteArrayOutputStream;

/**
 * An in-memory {@link java.io.OutputStream} that provides access to its internal buffer.
 */
public class ByteArrayBuffer extends ByteArrayOutputStream {
  /**
   * Creates a new buffer with the default initial size.
   */
  public ByteArrayBuffer() {
    super();
  }

  /**
   * Creates an new buffer with the given initial size.
   *
   * @param initialSize the initial size of the internal buffer.
   */
  public ByteArrayBuffer(int initialSize) {
    super(initialSize);
  }

  /**
   * Like {@link #toByteArray()}, but returns a reference to the internal buffer itself rather than
   * allocating more memory and returning a copy.
   *
   * @return the current contents of the internal buffer.
   */
  public byte[] getBuffer() {
    return buf;
  }

  /**
   * @return the currently-filled length of the internal buffer.
   */
  public int getLength() {
    return count;
  }

  /**
   * Logically adjusts the currently-filled length of internal buffer.
   *
   * @param length the newly desired length.
   */
  public void setLength(int length) {
    count = length;
  }
}
