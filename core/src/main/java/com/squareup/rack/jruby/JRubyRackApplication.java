package com.squareup.rack.jruby;

import com.squareup.rack.RackApplication;
import com.squareup.rack.RackInput;
import com.squareup.rack.RackResponse;
import com.squareup.rack.RackEnvironment;
import java.util.List;
import java.util.Map;
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

  public JRubyRackApplication(IRubyObject application) {
    this.application = application;
    this.runtime = application.getRuntime();
    this.threadService = runtime.getThreadService();
  }

  @Override public RackResponse call(RackEnvironment environment) {
    RubyHash environmentHash = convertToRubyHash(environment);

    RubyArray response = callRackApplication(environmentHash);

    return convertToJavaRackResponse(response);
  }

  private RubyHash convertToRubyHash(Map<String, Object> map) {
    RubyHash hash = newHash(runtime);

    for (Map.Entry<String, Object> entry : map.entrySet()) {
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
    Long status = (Long) response.get(0);
    Map headers = (Map) response.get(1);
    IRubyObject body = (IRubyObject) response.get(2);

    return new RackResponse(status.intValue(), headers, new JRubyRackBodyIterator(body));
  }
}
