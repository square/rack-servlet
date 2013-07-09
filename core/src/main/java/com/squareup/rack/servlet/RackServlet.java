package com.squareup.rack.servlet;

import com.squareup.rack.RackApplication;
import com.squareup.rack.RackEnvironment;
import com.squareup.rack.RackResponse;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class RackServlet extends HttpServlet {
  private final RackEnvironmentBuilder rackEnvironmentBuilder;
  private final RackApplication rackApplication;
  private final RackResponsePropagator rackResponsePropagator;

  public RackServlet(RackApplication rackApplication) {
    this(new RackEnvironmentBuilder(), rackApplication, new RackResponsePropagator());
  }

  @Inject public RackServlet(RackEnvironmentBuilder rackEnvironmentBuilder,
      RackApplication rackApplication,
      RackResponsePropagator rackResponsePropagator) {
    this.rackEnvironmentBuilder = rackEnvironmentBuilder;
    this.rackApplication = rackApplication;
    this.rackResponsePropagator = rackResponsePropagator;
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
