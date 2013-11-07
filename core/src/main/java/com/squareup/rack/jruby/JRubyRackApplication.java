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
package com.squareup.rack.jruby;

import com.squareup.rack.RackApplication;
import com.squareup.rack.RackEnvironment;
import com.squareup.rack.RackInput;
import com.squareup.rack.RackResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.internal.runtime.ThreadService;
import org.jruby.runtime.builtin.IRubyObject;

import static org.jruby.RubyHash.newHash;

/**
 * Adapts a (RubyObject) Rack application into Java-space.
 */
public class JRubyRackApplication implements RackApplication {
  private final IRubyObject application;
  private final Ruby runtime;
  private final ThreadService threadService;

  /**
   * <p>Creates a {@link RackApplication} that delegates to the given Ruby Rack application.</p>
   *
   * <p>To obtain the necessary {@link IRubyObject}, you can create a JRuby
   * {@link org.jruby.embed.ScriptingContainer} and {@link org.jruby.embed.ScriptingContainer#parse}
   * and {@link org.jruby.embed.EmbedEvalUnit#run()} your Ruby code. See our examples for concrete
   * code.</p>
   *
   * @param application the Ruby Rack application.
   */
  public JRubyRackApplication(IRubyObject application) {
    this.application = application;
    this.runtime = application.getRuntime();
    this.threadService = runtime.getThreadService();
  }

  /**
   * Calls the delegate Rack application, translating into and back out of the JRuby interpreter.
   *
   * @param environment the Rack environment
   * @return the Rack response
   */
  @Override public RackResponse call(RackEnvironment environment) {
    RubyHash environmentHash = convertToRubyHash(environment.entrySet());

    RubyArray response = callRackApplication(environmentHash);

    return convertToJavaRackResponse(response);
  }

  private RubyHash convertToRubyHash(Set<Map.Entry<String, Object>> entries) {
    RubyHash hash = newHash(runtime);

    for (Map.Entry<String, Object> entry : entries) {
      String key = entry.getKey();
      Object value = entry.getValue();

      if (key.equals("rack.input")) {
        value = new JRubyRackInput(runtime, (RackInput) value);
      }

      if (key.equals("rack.version")) {
        value = convertToRubyArray((List<Integer>) value);
      }

      hash.put(key, value);
    }

    return hash;
  }

  private RubyArray convertToRubyArray(List<Integer> list) {
    RubyArray array = RubyArray.newEmptyArray(runtime);
    array.addAll(list);
    return array;
  }

  private RubyArray callRackApplication(RubyHash rubyHash) {
    return (RubyArray) application.callMethod(threadService.getCurrentContext(), "call", rubyHash);
  }

  private RackResponse convertToJavaRackResponse(RubyArray response) {
    int status = Integer.parseInt(response.get(0).toString(), 10);
    Map headers = (Map) response.get(1);
    IRubyObject body = (IRubyObject) response.get(2);

    return new RackResponse(status, headers, new JRubyRackBodyIterator(body));
  }
}
