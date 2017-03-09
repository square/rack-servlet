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
package com.squareup.rack.servlet;

import com.squareup.rack.RackApplication;
import com.squareup.rack.RackEnvironment;
import com.squareup.rack.RackResponse;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Hosts a {@link RackApplication}.</p>
 *
 * <p>Since RackServlet lacks a zero-argument constructor, you'll need to manually instantiate and
 * install it in your container, rather than declaring it in a {@code web.xml} file. See our
 * examples for concrete code.</p>
 */
public class RackServlet extends HttpServlet {
  private final RackEnvironmentBuilder rackEnvironmentBuilder;
  private final RackApplication rackApplication;
  private final RackResponsePropagator rackResponsePropagator;

  /**
   * Creates a servlet hosting the given {@link RackApplication}.
   *
   * @param rackApplication the application to host.
   */
  public RackServlet(RackApplication rackApplication) {
    this(new RackEnvironmentBuilder(), rackApplication, new RackResponsePropagator());
  }

  /**
   * Creates a servlet hosting the given {@link RackApplication} and that uses the given
   * collaborators to translate between the Servlet and Rack environments.
   *
   * @param rackEnvironmentBuilder turns {@link HttpServletRequest}s into {@link RackEnvironment}s.
   * @param rackApplication the application to host.
   * @param rackResponsePropagator writes {@link RackResponse}s onto {@link HttpServletResponse}s.
   */
  public RackServlet(RackEnvironmentBuilder rackEnvironmentBuilder,
      RackApplication rackApplication,
      RackResponsePropagator rackResponsePropagator) {
    this.rackEnvironmentBuilder = rackEnvironmentBuilder;
    this.rackApplication = rackApplication;
    this.rackResponsePropagator = rackResponsePropagator;
  }

  @Override public void init() throws ServletException {
    this.rackApplication.setup(getServletContext());
  }

  @Override protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    RackEnvironment rackEnvironment = rackEnvironmentBuilder.build(request);

    try {
      RackResponse rackResponse = rackApplication.call(rackEnvironment);
      rackResponsePropagator.propagate(rackResponse, response);
    } finally {
      rackEnvironment.closeRackInput();
    }
  }
}
