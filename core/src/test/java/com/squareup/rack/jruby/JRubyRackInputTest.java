package com.squareup.rack.jruby;

import com.squareup.rack.RackInput;
import java.io.IOException;
import org.jruby.Ruby;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JRubyRackInputTest {
  @Test public void shouldWrapJavaIOExceptions() throws Exception {
    Ruby ruby = Ruby.newInstance();
    RackInput rackInput = mock(RackInput.class);
    when(rackInput.read(null)).thenThrow(new IOException("fake"));

    JRubyRackInput subject = new JRubyRackInput(ruby, rackInput);
    GlobalVariables globalVariables = ruby.getGlobalVariables();
    globalVariables.set("$rack_input", subject);

    IRubyObject result =
        ruby.evalScriptlet(
            "begin; $rack_input.read; rescue IOError => e; \"rescued #{e.message}\"; end");
    assertThat(result.asJavaString()).isEqualTo("rescued fake");
  }
}
