package com.squareup.rack.servlet;

import com.google.common.collect.ImmutableList;
import com.squareup.rack.RackErrors;
import com.squareup.rack.RackInput;
import com.squareup.rack.RackLogger;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.data.MapEntry.entry;

@RunWith(MockitoJUnitRunner.class)
public class RackEnvironmentBuilderTest {
  private TestHttpServletRequest.Builder request;
  private HttpServletRequest httpServletRequest;

  @Before public void setUp() {
    this.request = TestHttpServletRequest.newBuilder();
  }

  @Test public void requestMethod() {
    request.method("PUT");
    assertThat(environment()).contains(entry("REQUEST_METHOD", "PUT"));
  }

  @Test public void scriptNameAndPathInfoWheRequestingTheRoot() {
    request.uri("http://example.com/");
    assertThat(environment()).contains(entry("SCRIPT_NAME", ""));
    assertThat(environment()).contains(entry("PATH_INFO", "/"));
  }

  @Test public void scriptNameAndPathInfoWhenRequestingAPath() {
    request.uri("http://example.com/path/to/resource");
    assertThat(environment()).contains(entry("SCRIPT_NAME", ""));
    assertThat(environment()).contains(entry("PATH_INFO", "/path/to/resource"));
  }

  @Test public void scriptNameAndPathInfoWhenMounted() {
    request.uri("http://example.com/path/to/resource").whenMountedAt("/path/to/");
    assertThat(environment()).contains(entry("SCRIPT_NAME", "/path/to"));
    assertThat(environment()).contains(entry("PATH_INFO", "/resource"));
  }

  @Test public void queryString() {
    request.uri("http://example.com/");
    assertThat(environment()).contains(entry("QUERY_STRING", ""));
  }

  @Test public void queryStringGiven() {
    request.uri("http://example.com/?foo=bar");
    assertThat(environment()).contains(entry("QUERY_STRING", "foo=bar"));
  }

  @Test public void serverName() {
    request.uri("http://example.com/");
    assertThat(environment()).contains(entry("SERVER_NAME", "example.com"));
  }

  @Test public void serverPortHttp() {
    request.uri("http://example.com/");
    assertThat(environment()).contains(entry("SERVER_PORT", "80"));
  }

  @Test public void serverPortHttps() {
    request.uri("https://example.com/");
    assertThat(environment()).contains(entry("SERVER_PORT", "443"));
  }

  @Test public void serverPortGiven() {
    request.uri("http://example.com:1234/");
    assertThat(environment()).contains(entry("SERVER_PORT", "1234"));
  }

  @Test public void httpHeaders() {
    request.header("If-None-Match", "737060cd8c284d8af7ad3082f209582d");
    assertThat(environment()).contains(
        entry("HTTP_IF_NONE_MATCH", "737060cd8c284d8af7ad3082f209582d"));
  }

  @Test public void httpHeadersWithMultipleValues() {
    request.header("Accept", "text/plain").header("Accept", "text/html");
    assertThat(environment()).contains(entry("HTTP_ACCEPT", "text/plain,text/html"));
  }

  @Test public void httpHeadersContentLength() {
    request.header("Content-Length", "42");
    assertThat(environment()).contains(entry("CONTENT_LENGTH", "42"));
    assertThat(environment()).doesNotContainKey("HTTP_CONTENT_LENGTH");
  }

  @Test public void httpHeadersContentType() {
    request.header("Content-Type", "application/json");
    assertThat(environment()).contains(entry("CONTENT_TYPE", "application/json"));
    assertThat(environment()).doesNotContainKey("HTTP_CONTENT_TYPE");
  }

  @Test public void rackVersion() {
    assertThat(environment()).contains(entry("rack.version", ImmutableList.of(1, 2)));
  }

  @Test public void rackUrlSchemeHttp() {
    request.uri("http://example.com/");
    assertThat(environment()).contains(entry("rack.url_scheme", "http"));
  }

  @Test public void rackUrlSchemeHttps() {
    request.uri("https://example.com/");
    assertThat(environment()).contains(entry("rack.url_scheme", "https"));
  }

  @Test public void rackUrlSchemeHttpYelling() {
    request.uri("HTTP://EXAMPLE.COM/");
    assertThat(environment()).contains(entry("rack.url_scheme", "http"));
  }

  @Test public void rackInput() throws IOException {
    request.method("POST").body("foo=42&bar=0");
    assertThat(environment()).containsKey("rack.input");
    assertThat(environment().get("rack.input")).isInstanceOf(RackInput.class);
  }

  @Test public void rackErrors() {
    assertThat(environment()).containsKey("rack.errors");
    assertThat(environment().get("rack.errors")).isInstanceOf(RackErrors.class);
  }

  @Test public void rackLogger() {
    assertThat(environment()).containsKey("rack.logger");
    assertThat(environment().get("rack.logger")).isInstanceOf(RackLogger.class);
  }

  @Test public void rackMultithread() {
    assertThat(environment()).contains(entry("rack.multithread", true));
  }

  @Test public void rackMultiprocess() {
    assertThat(environment()).contains(entry("rack.multiprocess", true));
  }

  @Test public void rackRunOnce() {
    assertThat(environment()).contains(entry("rack.run_once", false));
  }

  @Test public void rackIsHijack() {
    assertThat(environment()).contains(entry("rack.hijack?", false));
  }

  @Test public void rackHijack() {
    assertThat(environment()).doesNotContainKey("rack.hijack");
  }

  @Test public void rackHijackIo() {
    assertThat(environment()).doesNotContainKey("rack.hijack_io");
  }

  @Test public void rackHttpServletRequest() {
    assertThat(environment()).containsKey("minecart.http_servlet_request");
    assertThat(environment().get("minecart.http_servlet_request")).isSameAs(httpServletRequest);
  }

  private Map<String, Object> environment() {
    RackEnvironmentBuilder environmentBuilder = new RackEnvironmentBuilder();
    httpServletRequest = request.build();
    return environmentBuilder.build(httpServletRequest);
  }
}
