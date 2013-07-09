package com.squareup.rack;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static com.squareup.rack.RackLogger.FATAL;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RackLoggerTest {
  private static final String MESSAGE = "message";

  private RackLogger subject;
  @Mock private Logger delegate;

  @Before public void setUp() {
    subject = new RackLogger(delegate);
  }

  @Test(expected = NullPointerException.class)
  public void constructorRequiresALogger() {
    new RackLogger(null);
  }

  @Test public void info() {
    subject.info(MESSAGE);
    verify(delegate).info(MESSAGE);
  }

  @Test public void debug() {
    subject.debug(MESSAGE);
    verify(delegate).debug(MESSAGE);
  }

  @Test public void warn() {
    subject.warn(MESSAGE);
    verify(delegate).warn(MESSAGE);
  }

  @Test public void error() {
    subject.error(MESSAGE);
    verify(delegate).error(MESSAGE);
  }

  @Test public void fatal() {
    subject.fatal(MESSAGE);
    verify(delegate).error(FATAL, MESSAGE);
  }
}
