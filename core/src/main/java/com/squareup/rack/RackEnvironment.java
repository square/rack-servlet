package com.squareup.rack;

import com.google.common.collect.ForwardingMap;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public class RackEnvironment extends ForwardingMap<String, Object> {
  public static final String REQUEST_METHOD = "REQUEST_METHOD";
  public static final String SCRIPT_NAME = "SCRIPT_NAME";
  public static final String PATH_INFO = "PATH_INFO";
  public static final String QUERY_STRING = "QUERY_STRING";
  public static final String SERVER_NAME = "SERVER_NAME";
  public static final String SERVER_PORT = "SERVER_PORT";
  public static final String CONTENT_LENGTH = "CONTENT_LENGTH";
  public static final String CONTENT_TYPE = "CONTENT_TYPE";
  public static final String HTTP_HEADER_PREFIX = "HTTP_";
  public static final String RACK_VERSION = "rack.version";
  public static final String RACK_URL_SCHEME = "rack.url_scheme";
  public static final String RACK_INPUT = "rack.input";
  public static final String RACK_ERRORS = "rack.errors";
  public static final String RACK_LOGGER = "rack.logger";
  public static final String RACK_MULTITHREAD = "rack.multithread";
  public static final String RACK_MULTIPROCESS = "rack.multiprocess";
  public static final String RACK_RUN_ONCE = "rack.run_once";
  public static final String RACK_HIJACK = "rack.hijack?";
  public static final String MINECART_HTTP_SERVLET_REQUEST = "minecart.http_servlet_request";

  private final Map<String,Object> delegate;

  public RackEnvironment(Map<String, Object> contents) {
    this.delegate = contents;
  }

  @Override protected Map<String, Object> delegate() {
    return delegate;
  }

  public void closeRackInput() throws IOException {
    ((Closeable) get(RACK_INPUT)).close();
  }
}
