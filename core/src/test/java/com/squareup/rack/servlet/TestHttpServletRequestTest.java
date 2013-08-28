package com.squareup.rack.servlet;

import org.junit.Before;
import org.junit.Test;

import static java.lang.String.format;
import static org.fest.assertions.api.Assertions.assertThat;

// Examples taken from http://account.pacip.com/jetty/doc/PathMapping.html
public class TestHttpServletRequestTest {
  private TestHttpServletRequest.Builder builder;

  @Before public void setUp() {
    builder = TestHttpServletRequest.newBuilder();
  }

  @Test public void absoluteMapping() {
    mount("/path");
    assertThat(get("/path").getServletPath()).isEqualTo("/path");
    assertThat(get("/path").getPathInfo()).isNull();
  }

  @Test public void prefixMapping_bare() {
    mount("/path/*");
    assertThat(get("/path").getServletPath()).isEqualTo("/path");
    assertThat(get("/path").getPathInfo()).isNull();
  }

  @Test public void prefixMapping_slash() {
    mount("/path/*");
    assertThat(get("/path/").getServletPath()).isEqualTo("/path");
    assertThat(get("/path/").getPathInfo()).isEqualTo("/");
  }

  @Test public void prefixMapping_slashPath() {
    mount("/path/*");
    assertThat(get("/path/info").getServletPath()).isEqualTo("/path");
    assertThat(get("/path/info").getPathInfo()).isEqualTo("/info");
  }

  @Test public void defaultMapping_slash() {
    mount("/");
    assertThat(get("/").getServletPath()).isEqualTo("");
    assertThat(get("/").getPathInfo()).isEqualTo("/");
  }

  @Test public void defaultMapping_slashPath() {
    mount("/");
    assertThat(get("/path").getServletPath()).isEqualTo("");
    assertThat(get("/path").getPathInfo()).isEqualTo("/path");
  }

  @Test public void defaultMapping_slashPathSlash() {
    mount("/");
    assertThat(get("/path/").getServletPath()).isEqualTo("");
    assertThat(get("/path/").getPathInfo()).isEqualTo("/path/");
  }

  @Test public void defaultMapping_slashPathSlashPath() {
    mount("/");
    assertThat(get("/path/info").getServletPath()).isEqualTo("");
    assertThat(get("/path/info").getPathInfo()).isEqualTo("/path/info");
  }

  private TestHttpServletRequest.Builder mount(String mountPath) {
    return builder.whenMountedAt(mountPath);
  }

  private TestHttpServletRequest get(String requestPath) {
    return builder.uri(format("%s%s", "http://example.com", requestPath)).build();
  }
}
