package com.twitter.schmemcached.java;

import com.twitter.schmemcached.protocol.Command;
import com.twitter.schmemcached.protocol.Response;
import com.twitter.util.Future;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.util.List;
import java.util.Map;

public abstract class Client {
  public static Client newInstance(com.twitter.finagle.service.Client<Command, Response> finagleClient) {
    com.twitter.schmemcached.Client schmemcachedClient = com.twitter.schmemcached.Client$.MODULE$.apply(finagleClient);
    return new ClientBase(schmemcachedClient);
  }

  abstract public Future<ChannelBuffer> get(String key);
  abstract public Future<Map<String, ChannelBuffer>> get(List<String> keys);
  abstract public Future<Response> set(String key, ChannelBuffer value);
  abstract public Future<Response> add(String key, ChannelBuffer value);
  abstract public Future<Response> append(String key, ChannelBuffer value);
  abstract public Future<Response> prepend(String key, ChannelBuffer value);
  abstract public Future<Response> delete(String key);
  abstract public Future<Integer> incr(String key);
  abstract public Future<Integer> incr(String key, int delta);
  abstract public Future<Integer> decr(String key);
  abstract public Future<Integer> decr(String key, int delta);

  public Future<Response> set(String key, String value) {
    return this.set(key, toChannelBuffer(value));
  }

  public Future<Response> add(String key, String value) {
    return this.set(key, toChannelBuffer(value));
  }

  public Future<Response> append(String key, String value) {
    return this.set(key, toChannelBuffer(value));
  }

  public Future<Response> prepend(String key, String value) {
    return this.set(key, toChannelBuffer(value));
  }

  private ChannelBuffer toChannelBuffer(String value) {
    return ChannelBuffers.wrappedBuffer(value.getBytes());
  }
}