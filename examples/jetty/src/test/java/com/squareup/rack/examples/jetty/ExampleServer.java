package com.squareup.rack.examples.jetty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.Servlet;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ExampleServer {
  private final Server server;

  public ExampleServer(Servlet servlet, String urlPattern) {
    ServletHolder holder = new ServletHolder(servlet);
    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(holder, urlPattern);
    server = new Server(0);
    server.setHandler(handler);
  }

  static HttpResponse get(HttpClient client, HttpHost localhost, String path) throws IOException {
    return client.execute(localhost, new HttpGet(path));
  }

  static InputStream streamOf(String contents) {
    return new ByteArrayInputStream(contents.getBytes());
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
