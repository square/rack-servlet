package com.squareup.rack.examples.dropwizard;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static com.google.common.io.Resources.getResource;
import static org.fest.assertions.api.Assertions.assertThat;

public class ExampleServiceTest {
  @ClassRule public static final DropwizardServiceRule<Configuration> RULE =
      new DropwizardServiceRule<Configuration>(ExampleService.class,
          getResource("example.yaml").getFile());

  private HttpClient client;
  private HttpHost localhost;

  @Before public void setUp() {
    client = new DefaultHttpClient();
    localhost = new HttpHost("localhost", RULE.getLocalPort());
  }

  @Test public void getRackHello() throws IOException {
    HttpResponse response = get("/rack/hello");
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(response.getEntity().getContent()).hasContentEqualTo(streamOf("Hello, World!"));
  }

  private HttpResponse get(String path) throws IOException {
    return client.execute(localhost, new HttpGet(path));
  }

  private InputStream streamOf(String contents) {
    return new ByteArrayInputStream(contents.getBytes());
  }
}
