package com.squareup.rack;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.copyOfRange;
import static org.fest.assertions.api.Assertions.assertThat;

public class RackInputTest {
  public static final byte[] EMPTY_BYTES = "".getBytes();
  public static final byte[] BYTES = "Hello,\nWorld!".getBytes();

  private RackInput empty;
  private RackInput full;
  private RackInput fullSlow;

  @Before public void setUp() throws Exception {
    empty = rackInputFor(EMPTY_BYTES);
    full = rackInputFor(BYTES);
    fullSlow = slowRackInputFor(BYTES);
  }

  @Test public void getsAtEof() throws Exception {
    assertThat(empty.gets()).isNull();
  }

  @Test public void gets() throws Exception {
    assertThat(full.gets()).isEqualTo("Hello,\n".getBytes());
  }

  @Test public void getsWithCrLf() throws Exception {
    assertThat(rackInputFor("Hello,\r\nWorld!").gets()).isEqualTo("Hello,\r\n".getBytes());
  }

  @Test public void getsAgain() throws Exception {
    full.gets();
    assertThat(full.gets()).isEqualTo("World!".getBytes());
  }

  @Test public void readWithLengthNilAtEof() throws Exception {
    assertThat(empty.read(null)).isEqualTo(EMPTY_BYTES);
  }

  @Test public void readWithLengthZeroAtEof() throws Exception {
    assertThat(empty.read(0)).isEqualTo(EMPTY_BYTES);
  }

  @Test public void readWithLengthAtEof() throws Exception {
    assertThat(empty.read(1)).isNull();
  }

  @Test public void readWithLengthNil() throws Exception {
    assertThat(full.read(null)).isEqualTo(BYTES);
  }

  @Test public void readWithLengthNilAgain() throws Exception {
    full.read(null);
    assertThat(full.read(null)).isEqualTo(EMPTY_BYTES);
  }

  @Test public void readWithLengthZero() throws Exception {
    assertThat(full.read(0)).isEqualTo(EMPTY_BYTES);
  }

  @Test public void readWithLength() throws Exception {
    assertThat(full.read(4)).isEqualTo(copyOfRange(BYTES, 0, 4));
  }

  @Test public void readWithLengthAgain() throws Exception {
    full.read(4);
    assertThat(full.read(4)).isEqualTo(copyOfRange(BYTES, 4, 8));
  }

  @Test public void readFromSlowStreamWithLength() throws Exception {
    assertThat(fullSlow.read(4)).isEqualTo(copyOfRange(BYTES, 0, 4));
  }

  @Test public void readFromSlowStreamWithLengthAgain() throws Exception {
    fullSlow.read(4);
    assertThat(fullSlow.read(4)).isEqualTo(copyOfRange(BYTES, 4, 8));
  }

  @Test public void readWithLengthTooLong() throws Exception {
    assertThat(full.read(BYTES.length + 1)).isEqualTo(BYTES);
  }

  @Test public void readWithLengthTooLongAgain() throws Exception {
    full.read(BYTES.length + 1);
    assertThat(full.read(BYTES.length + 1)).isNull();
  }

  @Test public void rewind() throws Exception {
    full.read(4);
    full.rewind();
    assertThat(full.read(4)).isEqualTo(copyOfRange(BYTES, 0, 4));
  }

  @Test public void rewind_shouldDiscardAnyBufferedBytes() throws Exception {
    RackInput subject = rackInputFor("first line\r\n123\r\n456\r\n");
    subject.gets();
    subject.rewind();
    assertThat(subject.gets()).isEqualTo("first line\r\n".getBytes());
    assertThat(subject.gets()).isEqualTo("123\r\n".getBytes());
  }

  @Test public void intermixingReadMethodsIsSafe() throws Exception {
    RackInput subject = rackInputFor("first line\r\n123\r\n456\r\n");
    assertThat(subject.read(1)).isEqualTo("f".getBytes());
    assertThat(subject.gets()).isEqualTo("irst line\r\n".getBytes());
    assertThat(subject.read(null)).isEqualTo("123\r\n456\r\n".getBytes());
    subject.rewind();
    assertThat(subject.gets()).isEqualTo("first line\r\n".getBytes());
    assertThat(subject.read(3)).isEqualTo("123".getBytes());
    assertThat(subject.gets()).isEqualTo("\r\n".getBytes());
    assertThat(subject.read(null)).isEqualTo("456\r\n".getBytes());
  }

  private RackInput rackInputFor(String string) throws Exception {
    return rackInputFor(string.getBytes());
  }

  private RackInput rackInputFor(byte[] bytes) throws Exception {
    return new RackInput(new ByteArrayInputStream(bytes));
  }

  private RackInput slowRackInputFor(byte[] bytes) throws Exception {
    return new RackInput(new SlowByteArrayInputStream(bytes));
  }
}
