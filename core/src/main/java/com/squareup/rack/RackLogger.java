package com.squareup.rack;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class RackLogger {
  public static final Marker FATAL = MarkerFactory.getMarker("FATAL");

  private final Logger delegate;

  public RackLogger(Logger delegate) {
    this.delegate = checkNotNull(delegate);
  }

  public void info(String message) {
    delegate.info(message);
  }

  public void debug(String message) {
    delegate.debug(message);
  }

  public void warn(String message) {
    delegate.warn(message);
  }

  public void error(String message) {
    delegate.error(message);
  }

  public void fatal(String message) {
    // See http://www.slf4j.org/faq.html#fatal
    delegate.error(FATAL, message);
  }
}
