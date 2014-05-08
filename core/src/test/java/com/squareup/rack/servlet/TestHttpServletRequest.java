package com.squareup.rack.servlet;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletInputStream;

import static com.google.common.base.Strings.emptyToNull;

public class TestHttpServletRequest extends NullHttpServletRequest {
  private final String method;
  private final String servletPath;
  private final URI uri;
  private final ListMultimap<String, String> headers;
  private final String body;
  private final Map<String, Object> attributes;

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private final HashMap<String, Object> attributes;
    private String method;
    private String servletPath;
    private URI requestUri;
    private ImmutableListMultimap.Builder<String, String> headersBuilder;
    private String body;

    private Builder() {
      method = "GET";
      servletPath = "";
      requestUri = URI.create("http://example.com/");
      headersBuilder = ImmutableListMultimap.builder();
      body = "";
      attributes = new HashMap<String, Object>();
    }

    public Builder body(String body) {
      this.body = body;
      return this;
    }

    public Builder method(String method) {
      this.method = method;
      return this;
    }

    private static final CharMatcher SLASH = CharMatcher.is('/');
    private static final CharMatcher STAR = CharMatcher.is('*');

    public Builder whenMountedAt(String path) {
      this.servletPath = SLASH.trimTrailingFrom(STAR.trimTrailingFrom(path));
      return this;
    }

    public Builder header(String key, String value) {
      headersBuilder.put(key, value);
      return this;
    }

    public Builder uri(String uri) {
      this.requestUri = URI.create(uri);
      return this;
    }

    public Builder attribute(String name, Object value) {
      attributes.put(name, value);
      return this;
    }

    public TestHttpServletRequest build() {
      return new TestHttpServletRequest(method, servletPath, requestUri, headersBuilder.build(),
          body, attributes);
    }
  }

  private TestHttpServletRequest(String method, String servletPath, URI uri,
      ListMultimap<String, String> headers, String body, Map<String, Object>
      attributes) {
    this.method = method;
    this.servletPath = servletPath;
    this.uri = uri;
    this.headers = headers;
    this.body = body;
    this.attributes = attributes;
  }

  @Override public String getMethod() {
    return method;
  }

  @Override public String getServletPath() {
    return servletPath;
  }

  @Override public String getPathInfo() {
    return emptyToNull(uri.getPath().substring(servletPath.length()));
  }

  @Override public String getQueryString() {
    return uri.getQuery();
  }

  @Override public String getServerName() {
    return uri.getHost();
  }

  @Override public int getServerPort() {
    int port = uri.getPort();
    String scheme = uri.getScheme();
    return (port > 0) ? port : (scheme.equals("https") ? 443 : 80);
  }

  @Override public String getScheme() {
    return uri.getScheme();
  }

  @Override public ServletInputStream getInputStream() throws IOException {
    final InputStream stream = new ByteArrayInputStream(body.getBytes());

    return new ServletInputStream() {
      @Override public int read() throws IOException {
        return stream.read();
      }
    };
  }

  @Override public Enumeration<String> getHeaderNames() {
    return Collections.enumeration(headers.keySet());
  }

  @Override public Enumeration<String> getHeaders(String name) {
    return Collections.enumeration(headers.get(name));
  }

  @Override public Object getAttribute(String name) {
    return attributes.get(name);
  }

  @Override public Enumeration<String> getAttributeNames() {
    final Iterator<String> iterator = attributes.keySet().iterator();

    return new Enumeration<String>() {
      public boolean hasMoreElements() {
        return iterator.hasNext();
      }

      public String nextElement() {
        return iterator.next();
      }
    };
  }
}
