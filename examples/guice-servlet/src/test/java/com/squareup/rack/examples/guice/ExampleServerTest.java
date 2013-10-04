package com.squareup.rack.examples.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.squareup.rack.jruby.JRubyRackApplication;
import com.squareup.rack.servlet.RackServlet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
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

    server = new ExampleServer(new RackModule());
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

  private static class ExampleServer {
    private final Server server;

    public ExampleServer(Module... modules) {
      ServletContextHandler handler = new ServletContextHandler();
      handler.setContextPath("/");
      handler.addEventListener(new GuiceInjectorServletContextListener(modules));
      handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
      handler.addServlet(DefaultServlet.class, "/");

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

  private static class GuiceInjectorServletContextListener extends GuiceServletContextListener {
    private final Injector injector;

    public GuiceInjectorServletContextListener(Module... modules) {
      injector = Guice.createInjector(modules);
    }

    @Override protected Injector getInjector() {
      return injector;
    }
  }

  private static class RackModule extends ServletModule {
    @Override protected void configureServlets() {
      serve("/*").with(RackServlet.class);
    }

    @Provides @Singleton RackServlet provideRackServlet(IRubyObject application) {
      return new RackServlet(new JRubyRackApplication(application));
    }

    @Provides IRubyObject provideApplication() {
      ScriptingContainer ruby = new ScriptingContainer();
      return ruby.parse("lambda { |env| [200, {}, ['Hello, World!']] }").run();
    }
  }
}