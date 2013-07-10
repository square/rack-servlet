/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.rack;

import com.google.common.collect.ForwardingMap;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * The HTTP request environment consumed by a {@link RackApplication}.
 *
 * @see <a href="http://rack.rubyforge.org/doc/SPEC.html">The Rack Specification</a>
 * @see com.squareup.rack.servlet.RackEnvironmentBuilder
 */
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

  private final Map<String, Object> contents;

  /**
   * Creates a {@link RackEnvironment} with the given contents.
   *
   * @see com.squareup.rack.servlet.RackEnvironmentBuilder
   * @param contents
   */
  public RackEnvironment(Map<String, Object> contents) {
    this.contents = contents;
  }

  /**
   * Closes the rack.input stream.
   *
   * @throws IOException
   */
  public void closeRackInput() throws IOException {
    ((Closeable) contents.get(RACK_INPUT)).close();
  }

  @Override protected Map<String, Object> delegate() {
    return contents;
  }
}
