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

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class RackLogger {
  public static final Marker FATAL = MarkerFactory.getMarker("FATAL");

  private final Logger delegate;

  public RackLogger(Logger delegate) {
    this.delegate = checkNotNull(delegate);
  }

  public void info(String message) {
    delegate.info(message);
  }

  public void debug(String message) {
    delegate.debug(message);
  }

  public void warn(String message) {
    delegate.warn(message);
  }

  public void error(String message) {
    delegate.error(message);
  }

  public void fatal(String message) {
    // See http://www.slf4j.org/faq.html#fatal
    delegate.error(FATAL, message);
  }
}
