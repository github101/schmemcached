package com.twitter.schmemcached.stress

import org.specs.Specification
import com.twitter.schmemcached.Server
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.schmemcached.protocol._
import com.twitter.util.RandomSocket
import com.twitter.schmemcached.protocol.text.Memcached
import com.twitter.finagle.Service
import com.twitter.schmemcached.util.ChannelBufferUtils._

object InterpreterServiceSpec extends Specification {
  "InterpreterService" should {
    var server: Server = null
    var client: Service[Command, Response] = null

    doBefore {
      val address = RandomSocket()
      server = new Server(address)
      server.start()
      client = ClientBuilder()
        .hosts("localhost:" + address.getPort)
        .codec(new Memcached)
        .build()
    }

    doAfter {
      server.stop()
    }

    "set & get" in {
      val _key   = "key"
      val value = "value"
      val start = System.currentTimeMillis
      (0 until 100) map { i =>
        val key = _key + i
        client(Delete(key))()
        client(Set(key, 0, 0, value))()
        client(Get(Seq(key)))() mustEqual Values(Seq(Value(key, value)))
      }
      val end = System.currentTimeMillis
      println("%d ms".format(end - start))
    }
  }
}
