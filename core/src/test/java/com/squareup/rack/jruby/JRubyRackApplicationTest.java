package com.squareup.rack.jruby;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.squareup.rack.RackApplication;
import com.squareup.rack.RackEnvironment;
import com.squareup.rack.RackResponse;
import com.squareup.rack.servlet.RackEnvironmentBuilder;
import com.squareup.rack.servlet.TestHttpServletRequest;
import java.util.Iterator;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.data.MapEntry.entry;

public class JRubyRackApplicationTest {
  private static final Joiner SPACE = Joiner.on(' ');

  private RackApplication app;
  private RackEnvironment env;

  @Before public void setUp() {
    IRubyObject callable = Ruby.getGlobalRuntime()
        .evalScriptlet("proc { |env| [200, {'Content-Type' => 'text/plain'}, env.keys] }");

    RackEnvironmentBuilder envBuilder = new RackEnvironmentBuilder();
    TestHttpServletRequest request = TestHttpServletRequest.newBuilder().build();
    app = new JRubyRackApplication(callable);
    env = envBuilder.build(request);
  }

  @Test public void callSetsTheResponseStatus() {
    RackResponse response = app.call(env);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test public void callSetsTheResponseHeaders() {
    RackResponse response = app.call(env);
    assertThat(response.getHeaders()).contains(entry("Content-Type", "text/plain"));
  }

  @Test public void callSetsTheResponseBody() {
    RackResponse response = app.call(env);

    ImmutableList.Builder<String> strings = ImmutableList.builder();

    Iterator<byte[]> bytes = response.getBody();
    while (bytes.hasNext()) {
      strings.add(new String(bytes.next()));
    }

    assertThat(SPACE.join(strings.build())).isEqualTo(SPACE.join(env.keySet()));
  }
}
