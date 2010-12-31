package com.twitter.schmemcached.protocol.text

import scala.Function.tupled
import com.twitter.schmemcached.protocol._
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import com.twitter.schmemcached.util.ChannelBufferUtils._

object ParseCommand extends Parser[Command] {
  import Parser.DIGITS
  private[this] val NOREPLY = copiedBuffer("noreply".getBytes)
  private[this] val SET     = copiedBuffer("set"    .getBytes)
  private[this] val ADD     = copiedBuffer("add"    .getBytes)
  private[this] val REPLACE = copiedBuffer("replace".getBytes)
  private[this] val APPEND  = copiedBuffer("append" .getBytes)
  private[this] val PREPEND = copiedBuffer("prepend".getBytes)
  private[this] val GET     = copiedBuffer("get"    .getBytes)
  private[this] val GETS    = copiedBuffer("gets"   .getBytes)
  private[this] val DELETE  = copiedBuffer("delete" .getBytes)
  private[this] val INCR    = copiedBuffer("incr"   .getBytes)
  private[this] val DECR    = copiedBuffer("decr"   .getBytes)

  private[this] val storageCommands = collection.Set(
    SET, ADD, REPLACE, APPEND, PREPEND)

  def needsData(tokens: Seq[ChannelBuffer]) = {
    val commandName = tokens.head
    val args = tokens.tail
    if (storageCommands.contains(commandName)) {
      validateStorageCommand(args, null)
      Some(tokens(4).toInt)
    } else None
  }

  def apply(tokens: Seq[ChannelBuffer], data: ChannelBuffer): Command = {
    val commandName = tokens.head
    val args = tokens.tail
    commandName match {
      case SET       => tupled(Set)(validateStorageCommand(args, data))
      case ADD       => tupled(Add)(validateStorageCommand(args, data))
      case REPLACE   => tupled(Replace)(validateStorageCommand(args, data))
      case APPEND    => tupled(Append)(validateStorageCommand(args, data))
      case PREPEND   => tupled(Prepend)(validateStorageCommand(args, data))
      case _         => throw new NonexistentCommand(commandName.toString)
    }
  }

  def apply(tokens: Seq[ChannelBuffer]): Command = {
    val commandName = tokens.head
    val args = tokens.tail
    commandName match {
      case GET     => Get(args)
      case GETS    => Get(args)
      case DELETE  => Delete(validateDeleteCommand(args))
      case INCR    => tupled(Incr)(validateArithmeticCommand(args))
      case DECR    => tupled(Decr)(validateArithmeticCommand(args))
      case _       => throw new NonexistentCommand(commandName.toString)
    }
  }

  private[this] def validateStorageCommand(tokens: Seq[ChannelBuffer], data: ChannelBuffer) = {
    if (tokens.size < 4) throw new ClientError("Too few arguments")
    if (tokens.size == 5 && tokens(4) != NOREPLY) throw new ClientError("Too many arguments")
    if (tokens.size > 5) throw new ClientError("Too many arguments")
    if (!tokens(3).matches(DIGITS)) throw new ClientError("Bad frame length")

    (tokens.head, tokens(1).toInt, tokens(2).toInt, data)
  }

  private[this] def validateArithmeticCommand(tokens: Seq[ChannelBuffer]) = {
    if (tokens.size < 2) throw new ClientError("Too few arguments")
    if (tokens.size == 3 && tokens.last != NOREPLY) throw new ClientError("Too many arguments")
    if (!tokens(1).matches(DIGITS)) throw new ClientError("Delta is not a number")

    (tokens.head, tokens(1).toInt)
  }

  private[this] def validateDeleteCommand(tokens: Seq[ChannelBuffer]) = {
    if (tokens.size < 1) throw new ClientError("No key")
    if (tokens.size == 2 && !tokens.last.matches(DIGITS)) throw new ClientError("Timestamp is poorly formed")
    if (tokens.size > 2) throw new ClientError("Too many arguments")

    tokens.head
  }
}