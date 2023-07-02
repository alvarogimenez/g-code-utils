package com.gomezgimenez.gcode.utils.services

import com.gomezgimenez.gcode.utils.entities._
import com.gomezgimenez.gcode.utils.entities.geometry._
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

  def rotateAndDisplace(
      gCode: Vector[GBlock],
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

  def gCodeToSegments(gCode: Vector[GBlock], x: Double = 0, y: Double = 0): Vector[Geometry] =
    gCode
      .foldLeft((Vector.empty[Geometry], GCommandMotion(0), x, y)) {
        case ((segments, motion, lastX, lastY), n) =>
          n match {
            case b: GCommandBlock if b.coordinateCommands.nonEmpty =>
              val x = b.coordinateCommands.find(_.coordinate == "X").map(_.value).getOrElse(lastX)
              val y = b.coordinateCommands.find(_.coordinate == "Y").map(_.value).getOrElse(lastY)
              val i = b.coordinateCommands.find(_.coordinate == "I").map(_.value)
              val j = b.coordinateCommands.find(_.coordinate == "J").map(_.value)
              val r = b.coordinateCommands.find(_.coordinate == "R").map(_.value)
              val m = b.motion.getOrElse(motion)

              val g: Geometry = m match {
                case GCommandMotion(0) =>
                  Segment(Point(lastX, lastY), Point(x, y), dotted = true)
                case GCommandMotion(0) =>
                  Segment(Point(lastX, lastY), Point(x, y))
                case GCommandMotion(index) if index == 2 || index == 3 =>
                  val offsetMode = i.flatMap(i => j.map((i, _)))
                  val radiusMode = r
                  offsetMode
                    .map {
                      case (i, j) =>
                        val from   = Point(lastX, lastY)
                        val ij     = Point(i, j)
                        val to     = Point(x, y)
                        val center = from + ij
                        val radius = ij.length()
                        val s1     = from - center
                        val s2     = to - center
                        val start  = -Math.atan2(s1.y, s1.x)
                        val startAngle = Math.toDegrees(if(start < 0) start + Math.PI*2 else start)
                        val ccw    = Math.atan2(s1.x * s2.y - s1.y * s2.x, s1.x * s2.x + s1.y * s2.y)
                        val ccwAngle = Math.toDegrees(if (ccw < 0) ccw + Math.PI * 2 else ccw)
                        val cw = Math.atan2(s1.y, s1.x) - Math.atan2(s2.y, s2.x)
                        val cwAngle = Math.toDegrees(if (cw < 0) cw + Math.PI * 2 else cw)

                        val (s, l) =
                          if (index == 2) {
                            (startAngle, cwAngle)
                          } else {
                            (startAngle - ccwAngle, ccwAngle)
                          }

                        /*println(s"${b.print}, " +
                          s"LAST ($lastX, $lastY), " +
                          s"s1 = $s1, " +
                          s"s2 = $s2, " +
                          s"C = $center, " +
                          s"startAngle = $startAngle, " +
                          s"cw = $cwAngle, " +
                          s"ccw = $ccwAngle, " +
                          s"s = $s, " +
                          s"len = $l"
                        )*/

                        Arc(center, radius, s, l, index)
                    }
                    .orElse(radiusMode.map { _ =>
                      // TODO: Arc radius mode is not supported
                      Segment(Point(lastX, lastY), Point(x,y))
                    })
                    .getOrElse(Segment(Point(lastX, lastY), Point(x, y)))
                case GCommandMotion(2) =>
                  Segment(Point(lastX, lastY), Point(x, y))
                case _ =>
                  Segment(Point(lastX, lastY), Point(x, y))
              }
              (segments :+ g, m, x, y)
            case b: GCommandBlock =>
              (segments, b.motion.getOrElse(motion), lastX, lastY)
            case _ =>
              (segments, motion, lastX, lastY)
          }
      }
      ._1

  def normalize(gCode: Vector[GBlock], command: Boolean, coordinates: Boolean): Vector[GBlock] =
    gCode
      .foldLeft((Vector.empty[GBlock], GCommandMotion(0), Option.empty[Double], Option.empty[Double], Option.empty[Double])) {
        case ((acc, motion, lastX, lastY, lastZ), n) =>
          n match {
            case b: GCommandBlock if b.coordinateCommands.nonEmpty =>
              val x = b.coordinateCommands.find(_.coordinate == "X").map(_.value).orElse(lastX)
              val y = b.coordinateCommands.find(_.coordinate == "Y").map(_.value).orElse(lastY)
              val z = b.coordinateCommands.find(_.coordinate == "Z").map(_.value).orElse(lastZ)
              val m = b.motion.getOrElse(motion)

              val normalizedGBlock =
                b.copy(
                  commands =
                  (if (command) List(m) else b.motion.toList) ++
                  (if (coordinates) {
                     x.map(GCommandCoordinate("X", _)).toList ++
                     y.map(GCommandCoordinate("Y", _)).toList ++
                     z.map(GCommandCoordinate("Z", _)).toList
                   } else List.empty[GCommand]) ++
                  b.commands.filterNot {
                    case _: GCommandMotion                                                                  => true
                    case c: GCommandCoordinate if List("X", "Y", "Z").contains(c.coordinate) && coordinates => true
                    case _                                                                                  => false
                  })
              (acc :+ normalizedGBlock, m, x, y, z)
            case b: GCommandBlock =>
              (acc :+ b, b.motion.getOrElse(motion), lastX, lastY, lastZ)
            case b =>
              (acc :+ b, motion, lastX, lastY, lastZ)
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
