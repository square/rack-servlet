package com.squareup.rack.examples.jetty;

import com.squareup.rack.jruby.JRubyRackApplication;
import com.squareup.rack.servlet.RackServlet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.Servlet;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY;

public class ExampleServerTest {
  private HttpClient client;
  private HttpHost localhost;
  private ExampleServer server;

  @Before public void setUp() throws Exception {
    // Silence logging.
    System.setProperty(DEFAULT_LOG_LEVEL_KEY, "WARN");

    // Build the Rack servlet.
    ScriptingContainer ruby = new ScriptingContainer();
    IRubyObject application = ruby.parse("lambda { |env| [200, {}, ['Hello, World!']] }").run();
    RackServlet servlet = new RackServlet(new JRubyRackApplication(application));

    server = new ExampleServer(servlet, "/*");
    server.start();
    client = new DefaultHttpClient();
    localhost = new HttpHost("localhost", server.getPort());
  }

  @After public void tearDown() throws Exception {
    server.stop();
  }

  @Test public void get() throws IOException {
    HttpResponse response = get("/anything");
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(response.getEntity().getContent()).hasContentEqualTo(streamOf("Hello, World!"));
  }

  private HttpResponse get(String path) throws IOException {
    return client.execute(localhost, new HttpGet(path));
  }

  private InputStream streamOf(String contents) {
    return new ByteArrayInputStream(contents.getBytes());
  }

  public static class ExampleServer {
    private final Server server;

    public ExampleServer(Servlet servlet, String urlPattern) {
      ServletHolder holder = new ServletHolder(servlet);
      ServletHandler handler = new ServletHandler();
      handler.addServletWithMapping(holder, urlPattern);
      server = new Server(0);
      server.setHandler(handler);
    }

    public void start() throws Exception {
      server.start();
    }

    public int getPort() {
      Connector[] connectors = server.getConnectors();
      NetworkConnector connector = (NetworkConnector) connectors[0];
      return connector.getLocalPort();
    }

    public void stop() throws Exception {
      server.stop();
    }
  }
}
