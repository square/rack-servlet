package com.squareup.rack.io;

import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.fest.assertions.api.Assertions.assertThat;

public class TempfileBufferedInputStreamTest {
  public @Rule TemporaryFolder tempDir = new TemporaryFolder();

  @Test public void readingAShortStreamTwice() throws IOException {
    InputStream subject = new TempfileBufferedInputStream(containing("Hello!"));
    subject.mark(10);
    assertThat(read(subject)).isEqualTo("Hello!");
    subject.reset();
    assertThat(read(subject)).isEqualTo("Hello!");
  }

  @Test public void readingALongStreamTwice() throws IOException {
    InputStream subject = new TempfileBufferedInputStream(containing("Hello!"), 3);
    subject.mark(10);
    assertThat(read(subject)).isEqualTo("Hello!");
    subject.reset();
    assertThat(read(subject)).isEqualTo("Hello!");
  }

  @Test public void shouldResetToMarkUnderThreshold() throws Exception {
    InputStream subject = new TempfileBufferedInputStream(containing("Hello!"));
    read(subject, 2);
    subject.mark(10);
    read(subject);
    subject.reset();
    assertThat(read(subject)).isEqualTo("llo!");
  }

  @Test public void shouldResetToMarkOverThreshold() throws Exception {
    InputStream subject = new TempfileBufferedInputStream(containing("Hello!"), 3);
    read(subject, 4);
    subject.mark(10);
    read(subject);
    subject.reset();
    assertThat(read(subject)).isEqualTo("o!");
  }

  @Test public void shouldSupportMultipleMarks() throws Exception {
    InputStream subject = new TempfileBufferedInputStream(containing("123456789"), 3);
    subject.mark(10); // set the mark at '1'
    read(subject, 4);
    subject.reset();
    read(subject, 4);
    subject.mark(10); // move the mark to '5'
    read(subject, 1);
    assertThat(read(subject)).isEqualTo("6789");

    subject.reset();
    assertThat(read(subject)).isEqualTo("56789");
  }

  @Test public void shouldNotLeaveTempFilesLingering() throws Exception {
    String originalTmpdir = System.getProperty("java.io.tmpdir");
    System.setProperty("java.io.tmpdir", tempDir.getRoot().toString());

    try {
      InputStream subject = new TempfileBufferedInputStream(containing("123456789"), 3);
      read(subject);
      assertThat(tempDir.getRoot().listFiles()).isEmpty();
    } finally {
      System.setProperty("java.io.tmpdir", originalTmpdir);
    }
  }

  @Test public void whenClosed_shouldCloseSourceStream() throws Exception {
    final List<String> log = new ArrayList<String>();
    InputStream source = new ByteArrayInputStream("bytes".getBytes()) {
      @Override public void close() throws IOException {
        log.add("closed InputStream");
        super.close();
      }
    };
    InputStream subject = new TempfileBufferedInputStream(source, 3);
    read(subject);
    subject.close();
    assertThat(log).contains("closed InputStream");
  }

  @Test public void whenClosed_shouldCloseTempFileStreamsIgnoringExceptions() throws Exception {
    final List<String> log = new ArrayList<String>();

    InputStream subject =
        new TempfileBufferedInputStream(containing("123456789"), 3) {
          @Override FileInputStream createFileInputStream(File tempFile)
              throws FileNotFoundException {
            return new FileInputStream(tempFile) {
              @Override public void close() throws IOException {
                log.add("closed FileInputStream");
                throw new IOException("fake exception");
              }
            };
          }

          @Override FileOutputStream createFileOutputStream(File tempFile)
              throws FileNotFoundException {
            return new FileOutputStream(tempFile) {
              @Override public void close() throws IOException {
                log.add("closed FileOutputStream");
                throw new IOException("fake exception");
              }
            };
          }
        };

    read(subject);
    subject.close();
    assertThat(log).contains("closed FileInputStream", "closed FileOutputStream");
  }

  private ByteArrayInputStream containing(String content) {
    return new ByteArrayInputStream(content.getBytes());
  }

  private String read(InputStream subject) throws IOException {
    return CharStreams.toString(new InputStreamReader(subject));
  }

  private void read(InputStream subject, int count) throws IOException {
    subject.read(new byte[count], 0, count);
  }
}
