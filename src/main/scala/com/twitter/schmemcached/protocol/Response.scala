package com.twitter.schmemcached.protocol

import org.jboss.netty.buffer.ChannelBuffer

sealed abstract class Response
case object NotFound                  extends Response
case object Stored                    extends Response
case object NotStored                 extends Response
case object Deleted                   extends Response

case class Values(values: Seq[Value]) extends Response
case class Number(value: Int)             extends Response

case class Value(key: ChannelBuffer, value: ChannelBuffer)