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
package com.squareup.rack;

import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts a {@link Logger} to the required interface for {@code rack.errors}.
 */
public class RackErrors {
  private final Logger logger;
  private final StringBuffer buffer;

  /**
   * Creates a {@link RackErrors} stream that forwards messages to the given {@link Logger}.
   *
   * @param logger the destination {@link Logger}.
   */
  public RackErrors(Logger logger) {
    this.logger = checkNotNull(logger);
    this.buffer = new StringBuffer();
  }

  /**
   * Immediately writes the given message out to the error logger.
   *
   * @param message
   */
  public void puts(String message) {
    logger.error(message);
  }

  /**
   * Buffers the given message internally. You may call {@link #write(String)} as many times as you
   * like. To then write the composite buffered message to the error logger, call {@link #flush()}.
   *
   * @param message
   */
  public void write(String message) {
    buffer.append(message);
  }

  /**
   * Writes internally-buffered messages out to the error logger.
   *
   * @see #write(String)
   */
  public void flush() {
    if (buffer.length() > 0) {
      logger.error(buffer.toString());
      buffer.setLength(0);
    }
  }
}
