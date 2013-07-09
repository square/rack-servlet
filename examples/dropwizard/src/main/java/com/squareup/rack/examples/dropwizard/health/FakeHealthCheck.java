package com.squareup.rack.examples.dropwizard.health;

import com.yammer.metrics.core.HealthCheck;

public class FakeHealthCheck extends HealthCheck {
  public FakeHealthCheck() {
    super("fake");
  }

  @Override protected Result check() throws Exception {
    return Result.healthy();
  }
}
