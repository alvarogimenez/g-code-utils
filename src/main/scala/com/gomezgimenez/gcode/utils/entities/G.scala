package com.gomezgimenez.gcode.utils.entities

import java.util.Locale

sealed trait GCommand {
  def print: String
}

case class GCommandCoordinate(coordinate: String, value: Double) extends GCommand {
  override def print: String = s"$coordinate${String.format(Locale.US, "%.3f", value)}"
}

case class GCommandMotion(index: Int)                            extends GCommand {
  override def print: String = s"G${String.format("%02d", index)}"
}

case class GCommandOther(command: String)                        extends GCommand {
  override def print: String = command
}

object GCommand {
  private val GCommandSyntax = """([A-Z])([0-9\-.]+)""".r

  def parse(cmd: String): Either[ParseError, GCommand] =
    cmd match {
      case GCommandSyntax(letter, payload) =>
        letter match {
          case "X" | "Y" | "Z" | "A" | "I" | "J" | "K" =>
            Right(GCommandCoordinate(letter, payload.toDouble))
          case "G" if List(0, 1, 2, 3).contains(payload.toInt) =>
            Right(GCommandMotion(payload.toInt))
          case _ =>
            Right(GCommandOther(cmd))
        }
      case _ =>
        Left(ParseError(cmd, s"Can't parse '$cmd' into a g-code command"))
    }
}

sealed trait GBlock {
  def print: String
}

case class GCommandBlock(commands: List[GCommand] = List.empty) extends GBlock {
  override def print: String = commands.map(_.print).mkString(" ")

  def coordinateCommands: List[GCommandCoordinate] = commands.collect {
    case c: GCommandCoordinate => c
  }

  def motion: Option[GCommandMotion] = commands.collectFirst {
    case c: GCommandMotion => c
  }
}

case class GCommentBlock(text: String)                          extends GBlock {
  override def print: String = text
}

case class GEmptyBlock()                          extends GBlock {
  override def print: String = ""
}

case class ParseError(payload: String, message: String)

object GBlock {
  private val Comment = """\(.+\)""".r

  def parse(line: String): Either[ParseError, GBlock] =
    line match {
      case Comment() =>
        Right(GCommentBlock(line))
      case line if line.trim.isEmpty =>
        Right(GEmptyBlock())
      case _ =>
        line
          .replaceAll(";.*", "")
          .split("(?=\\s*[A-Z])")
          .map(_.trim)
          .filter(_.nonEmpty)
          .map(GCommand.parse)
          .foldLeft[Either[ParseError, GCommandBlock]](Right(GCommandBlock())) { (acc, n) =>
            for {
              n <- n
              acc <- acc
            } yield acc.copy(commands = acc.commands :+ n)
          }
    }
}

object GParser {

  def parse(lines: Vector[String]): Either[ParseError, Vector[GBlock]] =
    lines
      .map(GBlock.parse)
      .foldLeft[Either[ParseError, Vector[GBlock]]](Right(Vector.empty)) { (acc,n) =>
        for {
          n <- n
          acc <- acc
        } yield acc :+ n
      }

}