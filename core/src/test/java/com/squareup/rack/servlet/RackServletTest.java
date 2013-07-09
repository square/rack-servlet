package com.squareup.rack.servlet;

import com.squareup.rack.RackApplication;
import com.squareup.rack.RackEnvironment;
import com.squareup.rack.RackResponse;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RackServletTest {
  private RackServlet subject;

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private RackApplication rackApplication;
  @Mock private RackEnvironmentBuilder rackEnvironmentBuilder;
  @Mock private RackEnvironment rackEnvironment;
  @Mock private RackResponse rackResponse;
  @Mock private RackResponsePropagator rackResponsePropagator;

  @Before public void setUp() {
    subject = new RackServlet(rackEnvironmentBuilder, rackApplication, rackResponsePropagator);
    when(rackEnvironmentBuilder.build(request)).thenReturn(rackEnvironment);
  }

  @Test public void service() throws ServletException, IOException {
    when(rackApplication.call(rackEnvironment)).thenReturn(rackResponse);

    subject.service(request, response);

    InOrder inOrder = inOrder(rackResponsePropagator, rackEnvironment);
    inOrder.verify(rackResponsePropagator).propagate(rackResponse, response);
    inOrder.verify(rackEnvironment).closeRackInput();
  }
}
