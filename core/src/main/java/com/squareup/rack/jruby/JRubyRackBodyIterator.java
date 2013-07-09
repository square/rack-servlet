package com.squareup.rack.jruby;

import com.google.common.collect.AbstractIterator;
import org.jruby.RubyEnumerator;
import org.jruby.RubyString;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * Adapts a (RubyObject) enumerable into Java-space.
 */
// TODO(matthewtodd): if the underlying body responds to close, we should close it after iteration.
class JRubyRackBodyIterator extends AbstractIterator<byte[]> {
  private final ThreadContext threadContext;
  private final RubyEnumerator enumerator;

  JRubyRackBodyIterator(IRubyObject body) {
    threadContext = body.getRuntime().getThreadService().getCurrentContext();
    enumerator = (RubyEnumerator) body.callMethod(threadContext, "to_enum");
  }

  @Override protected byte[] computeNext() {
    try {
      return ((RubyString) enumerator.callMethod(threadContext, "next")).getBytes();
    } catch (RaiseException e) {
      return endOfData();
    }
  }
}
