package com.squareup.rack.examples.dropwizard.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class EmptyResource {
  @GET public String get() {
    return "";
  }
}
