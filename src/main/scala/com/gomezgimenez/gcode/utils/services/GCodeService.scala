package com.gomezgimenez.gcode.utils.services

import com.gomezgimenez.gcode.utils.entities._
import com.gomezgimenez.gcode.utils.entities.geometry.{Geometry, Point, Segment}
import com.gomezgimenez.gcode.utils.model.SegmentsWithPower

import java.io.File
import java.util.Locale
import scala.io.Source

case class GCodeService() {
  def readGCodeFile(file: File): Either[ParseError, Vector[GBlock]] = {
    val source = Source.fromFile(file)
    val gCode =
      source
        .getLines()
        .toVector
    source.close()
    GParser.parse(gCode)
  }

  def rotateAndDisplace(gCode: Vector[GBlock],
                        cx: Double = 0.0,
                        cy: Double = 0.0,
                        r: Double = 0.0,
                        dx: Double = 0.0,
                        dy: Double = 0.0,
                        sx: Double = 1.0,
                        sy: Double = 1.0
                       ): Vector[GBlock] =
    gCode
      .foldLeft((Vector.empty[GBlock], GCommandMotion(0), 0.0, 0.0)) {
        case ((segments, motion, lastX, lastY), n) =>
          n match {
            case b: GCommandBlock if b.coordinateCommands.nonEmpty =>
              val x = b.coordinateCommands.find(_.coordinate == "X").map(_.value).getOrElse(lastX)
              val y = b.coordinateCommands.find(_.coordinate == "Y").map(_.value).getOrElse(lastY)
              val i = b.coordinateCommands.find(_.coordinate == "I").map(_.value).getOrElse(0.0)
              val j = b.coordinateCommands.find(_.coordinate == "J").map(_.value).getOrElse(0.0)

              val pxy = Point(x, y).rotate(r, Point(cx, cy)).translate(dx, dy) * Point(sx, sy)
              val pij = Point(i, j).rotate(r) * Point(sx, sy)

              val updatedAbsoluteCoordinates =
                if (b.coordinateCommands.exists(c => c.coordinate == "X" || c.coordinate == "Y")) {
                  List(
                    GCommandCoordinate("X", pxy.x),
                    GCommandCoordinate("Y", pxy.y),
                  )
                } else List.empty

              val updatedRelativeCoordinates =
                if (b.coordinateCommands.exists(c => c.coordinate == "I" || c.coordinate == "J")) {
                  List(
                    GCommandCoordinate("I", pij.x),
                    GCommandCoordinate("J", pij.y),
                  )
                } else List.empty

              val updatedCommands =
              b.commands
                .filterNot {
                  case x: GCommandCoordinate =>
                    List("X", "Y", "I", "J").contains(x.coordinate)
                  case _ =>
                    false
                } ++ updatedAbsoluteCoordinates ++ updatedRelativeCoordinates

              (segments :+ b.copy(commands = updatedCommands), motion, x, y)
            case b =>
              (segments :+ b, motion, lastX, lastY)
          }
      }
      ._1

  def gCodeToSegments(gCode: Vector[GBlock]): Vector[Geometry] =
    gCode
      .foldLeft((Vector.empty[Geometry], GCommandMotion(0), 0.0, 0.0)) {
        case ((segments, motion, lastX, lastY), n) =>
          n match {
            case b: GCommandBlock if b.coordinateCommands.nonEmpty =>
              val x = b.coordinateCommands.find(_.coordinate == "X").map(_.value).getOrElse(lastX)
              val y = b.coordinateCommands.find(_.coordinate == "Y").map(_.value).getOrElse(lastY)
              val m = b.motion.getOrElse(motion)

              (segments :+ Segment(Point(lastX, lastY), Point(x, y)),m , x, y)
            case b: GCommandBlock =>
              (segments,  b.motion.getOrElse(motion), lastX, lastY)
            case _ =>
              (segments,  motion, lastX, lastY)
          }
      }
      ._1

  def segmentsToLaserGCode(
      segmentsByPower: List[SegmentsWithPower],
      showBoxes: Boolean,
      showText: Boolean,
      feedRateXY: Double
  ): List[String] =
    List(
      "G21", // Metric system,
      "G90", // Absolute positioning
      "G17", // XY plane
      "G94", // Feed per minute
      "G00 Z0" // Set height to 0
    ) ++ segmentsByPower.flatMap { s =>
      val segments =
      (if (showBoxes) s.boxSegments else List.empty) ++
      (if (showText) s.textSegments else List.empty)
      segments
        .foldLeft[(Option[Segment], List[String])]((None, List.empty)) {
          case ((last, acc), n) =>
            val gCode = if (last.exists(_.p2 == n.p1)) {
              List(
                s"G01 X${String.format(Locale.US, "%.3f", n.p2.x)} " +
                s"Y${String.format(Locale.US, "%.3f", n.p2.y)}"
              )
            } else {
              List(
                "M5",
                s"G00 X${String.format(Locale.US, "%.3f", n.p1.x)} " +
                s"Y${String.format(Locale.US, "%.3f", n.p1.y)}",
                s"M03 S${String.format(Locale.US, "%.1f", s.power)}",
                s"G01 X${String.format(Locale.US, "%.3f", n.p2.x)} " +
                s"Y${String.format(Locale.US, "%.3f", n.p2.y)} " +
                s"F${String.format(Locale.US, "%.1f", feedRateXY)}"
              )
            }
            (Some(n), acc ++ gCode)
        }
        ._2
    } ++ List(
      "M5", // Stop laser
      "G00 X0Y0" // Return to XY origin
    )
}
