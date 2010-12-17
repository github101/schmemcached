package com.twitter.twemcached.protocol

import org.jboss.netty.buffer.ChannelBuffer

sealed abstract class Command

abstract class StorageCommand(key: ChannelBuffer, value: ChannelBuffer) extends Command
abstract class ArithmeticCommand(key: ChannelBuffer, delta: Int)        extends Command
abstract class RetrievalCommand(keys: Seq[ChannelBuffer])           extends Command

case class Set(key: ChannelBuffer, value: ChannelBuffer)                extends StorageCommand(key, value)
case class Add(key: ChannelBuffer, value: ChannelBuffer)                extends StorageCommand(key, value)
case class Replace(key: ChannelBuffer, value: ChannelBuffer)            extends StorageCommand(key, value)
case class Append(key: ChannelBuffer, value: ChannelBuffer)             extends StorageCommand(key, value)
case class Prepend(key: ChannelBuffer, value: ChannelBuffer)            extends StorageCommand(key, value)

case class Get(keys: Seq[ChannelBuffer])                            extends RetrievalCommand(keys)
case class Gets(keys: Seq[ChannelBuffer])                           extends RetrievalCommand(keys)

case class Delete(key: ChannelBuffer)                                   extends Command
case class Incr(key: ChannelBuffer, value: Int)                         extends ArithmeticCommand(key, value)
case class Decr(key: ChannelBuffer, value: Int)                         extends ArithmeticCommand(key, -value)