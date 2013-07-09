package com.squareup.rack.servlet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.squareup.rack.RackResponse;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RackResponsePropagatorTest {
  private RackResponsePropagator subject;
  private RackResponseBuilder rackResponse;

  @Mock private HttpServletResponse response;
  @Mock private ServletOutputStream outputStream;

  @Before public void setUp() throws IOException {
    subject = new RackResponsePropagator();
    rackResponse = new RackResponseBuilder();
    when(response.getOutputStream()).thenReturn(outputStream);
  }

  @Test public void propagateStatus() {
    rackResponse.status(404);
    subject.propagate(rackResponse.build(), response);
    verify(response).setStatus(404);
  }

  @Test public void propagateHeaders() {
    rackResponse.header("Content-Type", "text/plain");
    subject.propagate(rackResponse.build(), response);
    verify(response).addHeader("Content-Type", "text/plain");
  }

  @Test public void propagateHeadersSkipsHeadsRackHeaders() {
    rackResponse.header("rack.internal", "42");
    subject.propagate(rackResponse.build(), response);
    verify(response, never()).addHeader(eq("rack.internal"), anyString());
  }

  @Test public void propagateBody() throws IOException {
    rackResponse.body("Here ".getBytes(), "are ".getBytes(), "the ".getBytes(), "parts.".getBytes());

    subject.propagate(rackResponse.build(), response);

    InOrder inOrder = inOrder(outputStream);
    inOrder.verify(outputStream).write("Here ".getBytes());
    inOrder.verify(outputStream).write("are ".getBytes());
    inOrder.verify(outputStream).write("the ".getBytes());
    inOrder.verify(outputStream).write("parts.".getBytes());
    inOrder.verify(outputStream).flush();
  }

  private static class RackResponseBuilder {
    private int status;
    private final ImmutableMap.Builder<String, String> headers;
    private final ImmutableList.Builder<byte[]> body;

    public RackResponseBuilder() {
      this.status = 200;
      this.headers = ImmutableMap.builder();
      this.body = ImmutableList.builder();
    }

    public RackResponseBuilder status(int status) {
      this.status = status;
      return this;
    }

    public RackResponseBuilder header(String key, String value) {
      this.headers.put(key, value);
      return this;
    }

    public RackResponseBuilder body(byte[]... parts) {
      body.add(parts);
      return this;
    }

    public RackResponse build() {
      return new RackResponse(status, headers.build(), body.build().iterator());
    }
  }
}
