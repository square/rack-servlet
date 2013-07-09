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
