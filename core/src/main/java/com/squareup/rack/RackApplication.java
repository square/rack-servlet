package com.squareup.rack;

public interface RackApplication {
  RackResponse call(RackEnvironment environment);
}
