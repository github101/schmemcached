package com.twitter.schmemcached.java;

import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.schmemcached.protocol.Command;
import com.twitter.schmemcached.protocol.Response;
import com.twitter.schmemcached.protocol.text.Memcached;

import java.nio.charset.Charset;

public class ClientTest {
  public static void main(String[] args) {
    com.twitter.finagle.Service<Command, Response> service =
      ClientBuilder
        .get()
        .hosts("localhost:11211")
        .codec(new Memcached())
        .build();

    Client client = ClientBase.newInstance(service);
    client.delete("foo").get();
    client.set("foo", "bar").get();
    assert(client.get("foo").get().toString(Charset.defaultCharset()) == "bar");
  }
}
