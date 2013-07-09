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

import com.google.common.base.Throwables;
import com.squareup.rack.RackResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Translates RackResponses into HttpServletResponses.
 */
public class RackResponsePropagator {
  private static final String RACK_INTERNAL_HEADER_PREFIX = "rack.";

  public void propagate(RackResponse rackResponse, HttpServletResponse response) {
    propagateStatus(rackResponse, response);
    propagateHeaders(rackResponse, response);
    propagateBody(rackResponse, response);
  }

  private void propagateStatus(RackResponse rackResponse, HttpServletResponse response) {
    response.setStatus(rackResponse.getStatus());
  }

  private void propagateHeaders(RackResponse rackResponse, HttpServletResponse response) {
    for (Map.Entry<String, String> header : rackResponse.getHeaders().entrySet()) {
      if (shouldPropagateHeaderToClient(header)) {
        response.addHeader(header.getKey(), header.getValue());
      }
    }
  }

  private boolean shouldPropagateHeaderToClient(Map.Entry<String, String> header) {
    return !header.getKey().startsWith(RACK_INTERNAL_HEADER_PREFIX);
  }

  // TODO(matthewtodd): think more carefully through exception handling.
  private void propagateBody(RackResponse rackResponse, HttpServletResponse response) {
    ServletOutputStream outputStream = null;

    try {
      outputStream = response.getOutputStream();
    } catch (IOException e) {
      Throwables.propagate(e);
    }

    Iterator<byte[]> body = rackResponse.getBody();

    while (body.hasNext()) {
      try {
        outputStream.write(body.next());
      } catch (IOException e) {
        Throwables.propagate(e);
      }
    }

    try {
      outputStream.flush();
    } catch (IOException e) {
      Throwables.propagate(e);
    }
  }
}
