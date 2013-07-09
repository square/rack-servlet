package com.squareup.rack;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RackErrorsTest {
  private RackErrors rackErrors;

  @Mock private Logger logger;

  @Before public void setUp() {
    rackErrors = new RackErrors(logger);
  }

  @Test(expected = NullPointerException.class)
  public void constructorRequiresALogger() {
    new RackErrors(null);
  }

  @Test public void puts() {
    rackErrors.puts("Boom!");
    verify(logger).error("Boom!");
  }

  @Test public void write() {
    rackErrors.write("Boom?");
    verify(logger, never()).error(anyString());
  }

  @Test public void writeThenFlush() {
    rackErrors.write("Boom?");
    rackErrors.flush();
    verify(logger).error("Boom?");
  }

  @Test public void writeWriteWriteThenFlush() {
    rackErrors.write("Boom?");
    rackErrors.write("Boom!");
    rackErrors.write("Boom…");
    rackErrors.flush();
    verify(logger).error("Boom?Boom!Boom…");
  }

  @Test public void flushOnEmpty() {
    rackErrors.flush();
    verify(logger, never()).error(anyString());
  }

  @Test public void writeFlushWriteFlush() {
    rackErrors.write("A loooong message");
    rackErrors.flush();
    rackErrors.write("A short msg");
    rackErrors.flush();

    InOrder inOrder = inOrder(logger);
    inOrder.verify(logger).error("A loooong message");
    inOrder.verify(logger).error("A short msg");
  }
}
