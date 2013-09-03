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
