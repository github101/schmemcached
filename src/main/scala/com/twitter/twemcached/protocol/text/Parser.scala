package com.twitter.twemcached.protocol.text

import org.jboss.netty.buffer.{ChannelBufferIndexFinder, ChannelBuffer}
import org.jboss.netty.util.CharsetUtil
import collection.mutable.ArrayBuffer

object Parser {
  private[this] val SKIP_SPACE = 1
  val DIGITS = "^\\d+$"

  def tokenize(_buffer: ChannelBuffer) = {
    val tokens = new ArrayBuffer[String]
    var buffer = _buffer
    while (buffer.capacity > 0) {
      val tokenLength = buffer.bytesBefore(ChannelBufferIndexFinder.LINEAR_WHITESPACE)

      if (tokenLength < 0) {
        tokens += buffer.toString(CharsetUtil.US_ASCII)
        buffer = buffer.slice(0, 0)
      } else {
        tokens += buffer.slice(0, tokenLength).toString(CharsetUtil.US_ASCII)
        buffer = buffer.slice(tokenLength + SKIP_SPACE, buffer.capacity - tokenLength - SKIP_SPACE)
      }
    }
    tokens
  }
}

trait Parser[A] {
  def apply(tokens: Seq[String]): A
  def needsData(tokens: Seq[String]): Option[Int]
}