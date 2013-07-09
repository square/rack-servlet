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
 * Sends rack.error messages to the given logger.
 */
// TODO(matthewtodd): perhaps remove our (direct) dependency on square Logger.
// Delegate to a RackLogger? Then implement that as SquareRackLogger?
public class RackErrors {
  private final Logger logger;
  private final StringBuffer buffer;

  public RackErrors(Logger logger) {
    this.logger = checkNotNull(logger);
    this.buffer = new StringBuffer();
  }

  public void puts(String s) {
    logger.error(s);
  }

  public void write(String s) {
    buffer.append(s);
  }

  public void flush() {
    if (buffer.length() > 0) {
      logger.error(buffer.toString());
      buffer.setLength(0);
    }
  }
}
