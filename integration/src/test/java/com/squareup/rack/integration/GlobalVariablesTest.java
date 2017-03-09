package com.squareup.rack.integration;

import com.squareup.rack.jruby.JRubyRackApplication;
import com.squareup.rack.servlet.RackServlet;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.jruby.embed.PathType.CLASSPATH;
import static org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY;

/**
 * Created by andi on 25/03/14.
 */
public class GlobalVariablesTest {

  private HttpClient client;
  private HttpHost localhost;
  private ExampleServer server;

  @Before
  public void setUp() throws Exception {
    // Silence logging.
    System.setProperty(DEFAULT_LOG_LEVEL_KEY, "WARN");

    // Build the Rack servlet.
    ScriptingContainer ruby = new ScriptingContainer();
    IRubyObject application = ruby.parse(CLASSPATH, "application.rb").run();
    RackServlet servlet = new RackServlet(new JRubyRackApplication(application));

    server = new ExampleServer(servlet, "/*");
    server.start();
    client = new DefaultHttpClient();
    localhost = new HttpHost("localhost", server.getPort());
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void setsGlobalVariables() throws IOException {
    HttpResponse response = get("/global_vars");
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
  }

  private HttpResponse get(String path) throws IOException {
    return client.execute(localhost, new HttpGet(path));
  }
}
