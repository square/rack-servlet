package com.squareup.rack.examples.jetty;

import com.squareup.rack.jruby.JRubyRackApplication;
import com.squareup.rack.servlet.RackServlet;
import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
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
    HttpResponse response = ExampleServer.get(client, localhost, "/anything");
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(response.getEntity().getContent()).hasContentEqualTo(
        ExampleServer.streamOf("Hello, World!"));
  }

  @Test public void get_atRootMountedURIs() throws IOException {
    HttpResponse response = ExampleServer.get(client, localhost, "/");
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(response.getEntity().getContent()).hasContentEqualTo(
        ExampleServer.streamOf("Hello, World!"));
  }
}
