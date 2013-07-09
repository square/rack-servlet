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
