package com.twitter.schmemcached.protocol.text

import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import com.twitter.schmemcached.protocol._
import com.twitter.schmemcached.util.ChannelBufferUtils._
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}

object Show {
  private[this] val DELIMETER     = "\r\n"   .getBytes
  private[this] val VALUE         = "VALUE"  .getBytes
  private[this] val ZERO          = "0"      .getBytes
  private[this] val SPACE         = " "      .getBytes
  private[this] val GET           = "get"    .getBytes
  private[this] val DELETE        = "delete" .getBytes
  private[this] val INCR          = "incr"   .getBytes
  private[this] val DECR          = "decr"   .getBytes
  private[this] val ADD           = "add"    .getBytes
  private[this] val SET           = "set"    .getBytes
  private[this] val APPEND        = "append" .getBytes
  private[this] val PREPEND       = "prepend".getBytes
  private[this] val REPLACE       = "replace".getBytes
  private[this] val END           = "END"    .getBytes

  private[this] val STORED        = copiedBuffer("STORED".getBytes,       DELIMETER)
  private[this] val NOT_STORED    = copiedBuffer("NOT_STORED".getBytes,   DELIMETER)
  private[this] val EXISTS        = copiedBuffer("EXISTS".getBytes,       DELIMETER)
  private[this] val NOT_FOUND     = copiedBuffer("NOT_FOUND".getBytes,    DELIMETER)
  private[this] val DELETED       = copiedBuffer("DELETED".getBytes,      DELIMETER)

  private[this] val ERROR         = copiedBuffer("ERROR".getBytes,        DELIMETER)
  private[this] val CLIENT_ERROR  = copiedBuffer("CLIENT_ERROR".getBytes, DELIMETER)
  private[this] val SERVER_ERROR  = copiedBuffer("SERVER_ERROR".getBytes, DELIMETER)

  def apply(response: Response) = {
    response match {
      case Stored         => STORED
      case NotStored      => NOT_STORED
      case Deleted        => DELETED
      case Number(value)  =>
        val buffer = ChannelBuffers.dynamicBuffer(10)
        buffer.writeBytes(value.toString.getBytes)
        buffer
      case Values(values) =>
        val buffer = ChannelBuffers.dynamicBuffer(100 * values.size)
        val shown = values map { case Value(key, value) =>
          buffer.writeBytes(VALUE)
          buffer.writeBytes(SPACE)
          buffer.writeBytes(key)
          buffer.writeBytes(SPACE)
          buffer.writeBytes(ZERO)
          buffer.writeBytes(SPACE)
          buffer.writeBytes(value.capacity.toString.getBytes)
          buffer.writeBytes(DELIMETER)
          value.resetReaderIndex()
          buffer.writeBytes(value)
          buffer.writeBytes(DELIMETER)
        }
        buffer.writeBytes(END)
        buffer.writeBytes(DELIMETER)
        buffer
    }
  }

  def apply(command: Command): ChannelBuffer = {
    command match {
      case Add(key, value) =>
        showStorageCommand(ADD, key, value)
      case Set(key, value) =>
        showStorageCommand(SET, key, value)
      case Replace(key, value) =>
        showStorageCommand(REPLACE, key, value)
      case Append(key, value) =>
        showStorageCommand(APPEND, key, value)
      case Prepend(key, value) =>
        showStorageCommand(PREPEND, key, value)
      case Get(keys) =>
        apply(Gets(keys))
      case Gets(keys) =>
        val buffer = ChannelBuffers.dynamicBuffer(50 + 10 * keys.size)
        buffer.writeBytes(GET)
        buffer.writeBytes(SPACE)
        keys.foreach { key =>
          buffer.writeBytes(key)
          buffer.writeBytes(SPACE)
        }
        buffer.writeBytes(DELIMETER)
        buffer
      case Incr(key, amount) =>
        val buffer = ChannelBuffers.dynamicBuffer(50)
        buffer.writeBytes(INCR)
        buffer.writeBytes(SPACE)
        buffer.writeBytes(key)
        buffer.writeBytes(SPACE)
        buffer.writeBytes(amount.toString.getBytes)
        buffer.writeBytes(SPACE)
        buffer.writeBytes(DELIMETER)
        buffer
      case Decr(key, amount) =>
        val buffer = ChannelBuffers.dynamicBuffer(30)
        buffer.writeBytes(DECR)
        buffer.writeBytes(SPACE)
        buffer.writeBytes(key)
        buffer.writeBytes(SPACE)
        buffer.writeBytes(amount.toString.getBytes)
        buffer.writeBytes(SPACE)
        buffer.writeBytes(DELIMETER)
        buffer
      case Delete(key) =>
        val buffer = ChannelBuffers.dynamicBuffer(30)
        buffer.writeBytes(DELETE)
        buffer.writeBytes(SPACE)
        buffer.writeBytes(key)
        buffer.writeBytes(DELIMETER)
        buffer
    }
  }

  def apply(throwable: Throwable) = throwable match {
    case e: NonexistentCommand => ERROR
    case e: ClientError        => CLIENT_ERROR
    case e: ServerError        => SERVER_ERROR
    case _                     => throw throwable
  }

  @inline private[this] def showStorageCommand(name: Array[Byte], key: ChannelBuffer, value: ChannelBuffer) = {
    val buffer = ChannelBuffers.dynamicBuffer(50 + value.capacity)
    buffer.writeBytes(name)
    buffer.writeBytes(SPACE)
    buffer.writeBytes(key)
    buffer.writeBytes(SPACE)
    buffer.writeBytes(ZERO)
    buffer.writeBytes(SPACE)
    buffer.writeBytes(ZERO)
    buffer.writeBytes(SPACE)
    buffer.writeBytes(value.capacity.toString.getBytes)
    buffer.writeBytes(DELIMETER)
    buffer.writeBytes(value)
    buffer.writeBytes(DELIMETER)
    buffer
  }
}