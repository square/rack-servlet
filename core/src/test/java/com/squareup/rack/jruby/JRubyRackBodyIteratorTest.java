package com.squareup.rack.jruby;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class JRubyRackBodyIteratorTest {
  private ScriptingContainer scriptingContainer;

  @Before public void setUp() {
    scriptingContainer = new ScriptingContainer();
    scriptingContainer.runScriptlet(PathType.CLASSPATH, "enumerable_with_close.rb");
  }

  @Test public void iteratingOverAThingThatRespondsToClose_shouldCloseTheThing() {
    IRubyObject body = scriptingContainer.parse("EnumerableWithClose.new(%w(first second third))").run();
    JRubyRackBodyIterator subject = new JRubyRackBodyIterator(body);

    assertThat(isOpen(body)).isEqualTo(true);

    while (subject.hasNext()) {
      subject.next();
    }

    assertThat(isOpen(body)).isEqualTo(false);
  }

  @Test public void iteratingOverAThingThatDoesNotRespondToClose_shouldNotBlowUp() {
    JRubyRackBodyIterator subject = new JRubyRackBodyIterator(
        scriptingContainer.parse("%w(first second third)").run());

    while (subject.hasNext()) {
      subject.next();
    }
  }

  private Boolean isOpen(IRubyObject body) {
    return (Boolean) body.callMethod(body.getRuntime().getCurrentContext(), "open").toJava(Boolean.class);
  }
}
