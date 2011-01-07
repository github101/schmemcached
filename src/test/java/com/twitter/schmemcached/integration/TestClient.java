package com.twitter.schmemcached.integration;

import com.twitter.finagle.service.Service;
import com.twitter.schmemcached.java.Client;
import com.twitter.schmemcached.java.ClientBase;
import com.twitter.schmemcached.protocol.Command;
import com.twitter.schmemcached.protocol.Response;
import com.twitter.schmemcached.protocol.text.Memcached;
import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.schmemcached.util.ChannelBufferUtils;
import junit.framework.TestCase;
import org.jboss.netty.util.CharsetUtil;

import java.nio.charset.Charset;

public class TestClient extends TestCase {
  public static void main(String[] args) {
    System.out.println(new TestClient().run().wasSuccessful());
  }

  public void testGetAndSet() {
    com.twitter.finagle.service.Client<Command, Response> service =
      ClientBuilder
        .get()
        .hosts("localhost:11211")
        .codec(new Memcached())
        .buildService();

    Client client = ClientBase.newInstance(service);
    client.delete("foo").get();
    client.set("foo", "bar").get();
    System.out.println("hello?");
    client.get("foo").get().toString(Charset.defaultCharset());
  }
}
