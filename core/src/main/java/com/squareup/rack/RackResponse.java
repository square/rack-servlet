package com.squareup.rack;

import java.util.Iterator;
import java.util.Map;

public class RackResponse {
  private final int status;
  private final Map<String, String> headers;
  private final Iterator<byte[]> body;

  public RackResponse(int status, Map<String, String> headers, Iterator<byte[]> body) {
    this.status = status;
    this.headers = headers;
    this.body = body;
  }

  public int getStatus() {
    return status;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public Iterator<byte[]> getBody() {
    return body;
  }
}
