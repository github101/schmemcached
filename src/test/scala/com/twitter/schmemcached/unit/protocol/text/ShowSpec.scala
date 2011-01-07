package com.twitter.schmemcached.unit.protocol.text

import org.specs.Specification
import com.twitter.schmemcached.protocol.Add
import com.twitter.schmemcached.protocol.text.Show
import org.jboss.netty.util.CharsetUtil
import com.twitter.schmemcached.util.ChannelBufferUtils._

object ShowSpec extends Specification {
  "Show" should {
    "show commands" in {
      val value = "value"
      Show(Add("key", 1, 2, value)).toString(CharsetUtil.UTF_8) mustEqual "add key 1 2 5\r\nvalue\r\n"
    }
  }
}