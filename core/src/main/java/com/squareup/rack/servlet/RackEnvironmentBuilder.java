package com.squareup.rack.servlet;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.squareup.rack.RackEnvironment;
import com.squareup.rack.RackErrors;
import com.squareup.rack.RackInput;
import com.squareup.rack.RackLogger;
import com.squareup.rack.io.TempfileBackedInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterators.forEnumeration;
import static com.squareup.rack.RackEnvironment.CONTENT_LENGTH;
import static com.squareup.rack.RackEnvironment.CONTENT_TYPE;
import static com.squareup.rack.RackEnvironment.HTTP_HEADER_PREFIX;
import static com.squareup.rack.RackEnvironment.MINECART_HTTP_SERVLET_REQUEST;
import static com.squareup.rack.RackEnvironment.PATH_INFO;
import static com.squareup.rack.RackEnvironment.QUERY_STRING;
import static com.squareup.rack.RackEnvironment.RACK_ERRORS;
import static com.squareup.rack.RackEnvironment.RACK_HIJACK;
import static com.squareup.rack.RackEnvironment.RACK_INPUT;
import static com.squareup.rack.RackEnvironment.RACK_LOGGER;
import static com.squareup.rack.RackEnvironment.RACK_MULTIPROCESS;
import static com.squareup.rack.RackEnvironment.RACK_MULTITHREAD;
import static com.squareup.rack.RackEnvironment.RACK_RUN_ONCE;
import static com.squareup.rack.RackEnvironment.RACK_URL_SCHEME;
import static com.squareup.rack.RackEnvironment.RACK_VERSION;
import static com.squareup.rack.RackEnvironment.REQUEST_METHOD;
import static com.squareup.rack.RackEnvironment.SCRIPT_NAME;
import static com.squareup.rack.RackEnvironment.SERVER_NAME;
import static com.squareup.rack.RackEnvironment.SERVER_PORT;

/**
 * Transforms an HttpServletRequest into a Rack environment hash.
 *
 * @see <a href="http://rack.rubyforge.org/doc/SPEC.html">The Rack Specification</a>
 * @see <a href="https://tools.ietf.org/html/rfc3875#section-4.1.18">RFC 3875, section 4.1.18</a>
 * @see <a href="http://blog.phusion.nl/2013/01/23/the-new-rack-socket-hijacking-api/">The Rack
 * socket hijacking API</a>
 */
public class RackEnvironmentBuilder {
  // We conform to version 1.2 of the Rack specification.
  // Note that this number is completely different than the gem version of rack (lowercase):
  // for example, the rack-1.5.2 gem ships with handlers that conform to version 1.2 of the Rack
  // specification.
  private static final List<Integer> VERSION_1_2 = ImmutableList.of(1, 2);

  private static final Logger RACK_ERRORS_LOGGER = LoggerFactory.getLogger(RackErrors.class);
  private static final Logger RACK_LOGGER_LOGGER = LoggerFactory.getLogger(RackLogger.class);

  private static final Joiner COMMA = Joiner.on(',');
  private static final CharMatcher DASH = CharMatcher.is('-');

  public RackEnvironment build(HttpServletRequest request) {
    ImmutableMap.Builder<String, Object> content = ImmutableMap.builder();

    content.put(REQUEST_METHOD, request.getMethod());
    content.put(SCRIPT_NAME, request.getServletPath());
    content.put(PATH_INFO, request.getPathInfo());
    content.put(QUERY_STRING, nullToEmpty(request.getQueryString()));
    content.put(SERVER_NAME, request.getServerName());
    content.put(SERVER_PORT, String.valueOf(request.getServerPort()));
    content.put(RACK_VERSION, VERSION_1_2);
    content.put(RACK_URL_SCHEME, request.getScheme().toLowerCase());
    content.put(RACK_INPUT, rackInput(request));
    content.put(RACK_ERRORS, new RackErrors(RACK_ERRORS_LOGGER));
    content.put(RACK_LOGGER, new RackLogger(RACK_LOGGER_LOGGER));
    content.put(RACK_MULTITHREAD, true);
    content.put(RACK_MULTIPROCESS, true);
    content.put(RACK_RUN_ONCE, false);
    content.put(RACK_HIJACK, false);

    // Extra things we add that aren't in the Rack specification:
    content.put(MINECART_HTTP_SERVLET_REQUEST, request);

    Enumeration<String> headerNames = request.getHeaderNames();

    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      content.put(rackHttpHeaderKey(name), COMMA.join(forEnumeration(request.getHeaders(name))));
    }

    return new RackEnvironment(content.build());
  }

  private RackInput rackInput(HttpServletRequest request) {
    try {
      return new RackInput(new TempfileBackedInputStream(request.getInputStream()));
    } catch (IOException e) {
      throw propagate(e);
    }
  }

  private String rackHttpHeaderKey(String headerName) {
    String transformed = DASH.replaceFrom(headerName.toUpperCase(), "_");

    if (transformed.equals(CONTENT_LENGTH) || transformed.equals(CONTENT_TYPE)) {
      return transformed;
    } else {
      return HTTP_HEADER_PREFIX + transformed;
    }
  }
}
