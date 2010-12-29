package com.twitter.schmemcached.integration

import org.specs.Specification
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.schmemcached.protocol._
import com.twitter.schmemcached.protocol.text.Memcached
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.schmemcached.Client
import org.jboss.netty.util.CharsetUtil
import com.twitter.schmemcached.util.ChannelBufferUtils._

object ClientSpec extends Specification {
  /**
   * Note: This test needs a real Memcached server running on 11211 to work!!
   */
  "ConnectedClient" should {
    "simple client" in {
      val service = ClientBuilder()
        .hosts("localhost:11211")
        .codec(new Memcached)
        .buildService[Command, Response]()
      val client = Client(service)

      client.delete("foo")()

      "set & get" in {
        client.get("foo")() mustEqual None
        client.set("foo", "bar")()
        client.get("foo")().get.toString(CharsetUtil.UTF_8) mustEqual "bar"
      }

      "gets" in {
        client.set("foo", "bar")()
        client.set("baz", "boing")()
        val result = client.get(Seq("foo", "baz", "notthere"))()
          .map { case (key, value) => (key, value.toString(CharsetUtil.UTF_8)) }
        result mustEqual Map(
          "foo" -> "bar",
          "baz" -> "boing"
        )
      }

      "append & prepend" in {
        client.set("foo", "bar")()
        client.append("foo", "rab")()
        client.get("foo")().get.toString(CharsetUtil.UTF_8) mustEqual "barrab"
        client.prepend("foo", "rab")()
        client.get("foo")().get.toString(CharsetUtil.UTF_8) mustEqual "rabbarrab"
      }

      "incr & decr" in {
        client.set("foo", "")()
        client.incr("foo")()    mustEqual 1
        client.incr("foo", 2)() mustEqual 3
        client.decr("foo")()    mustEqual 2
      }
    }

    "partitioned client" in {
      val service1 = ClientBuilder()
        .name("service1")
        .hosts("localhost:11211")
        .codec(new Memcached)
        .buildService[Command, Response]()
      val service2 = ClientBuilder()
        .name("service2")
        .hosts("localhost:11212")
        .codec(new Memcached)
        .buildService[Command, Response]()
      val client = Client(Seq(service1, service2))

      client.delete("foo")()


      "doesn't blow up" in {
        client.get("foo")() mustEqual None
        client.set("foo", "bar")()
        client.get("foo")().get.toString(CharsetUtil.UTF_8) mustEqual "bar"
      }
    }
  }
}