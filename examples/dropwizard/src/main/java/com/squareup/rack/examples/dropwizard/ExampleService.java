package com.squareup.rack.examples.dropwizard;

import com.google.common.io.Resources;
import com.squareup.rack.examples.dropwizard.health.FakeHealthCheck;
import com.squareup.rack.examples.dropwizard.resources.EmptyResource;
import com.squareup.rack.jruby.JRubyRackApplication;
import com.squareup.rack.servlet.RackServlet;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import java.io.IOException;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;

public class ExampleService extends Service<Configuration> {
  @Override public void initialize(Bootstrap<Configuration> bootstrap) {
    // Nothing to do here.
  }

  @Override public void run(Configuration config, Environment environment) {
    // Suppress the "THIS SERVICE HAS NO HEALTHCHECKS" warning.
    // A real service would have proper health checks.
    environment.addHealthCheck(new FakeHealthCheck());

    // Suppress the "ResourceConfig instance does not contain any root resource classes" error.
    // A real service would probably provide a Jersey resource or two.
    environment.addResource(EmptyResource.class);

    // Here's the interesting part:
    // Mount the Rack application defined in the config.ru file on the classpath at /rack.
    environment.addServlet(createRackServlet(), "/rack/*");
  }

  private RackServlet createRackServlet() {
    return new RackServlet(new JRubyRackApplication(createApplication()));
  }

  private IRubyObject createApplication() {
    // There's a lot you could do here; for now, we just read a rackup file from the classpath,
    // then build a Rack application based on it.
    ScriptingContainer container = new ScriptingContainer();
    container.put("builder_script", readResource("config.ru"));
    return container.parse("require 'rack'; Rack::Builder.new_from_string(builder_script)").run();
  }

  private String readResource(String path) {
    try {
      return Resources.toString(getResource(path), defaultCharset());
    } catch (IOException e) {
      throw propagate(e);
    }
  }
}
