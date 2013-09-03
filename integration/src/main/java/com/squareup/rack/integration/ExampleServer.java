package com.squareup.rack.integration;

import javax.servlet.Servlet;
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
