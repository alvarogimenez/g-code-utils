package com.gomezgimenez.gcode.utils.services

import java.io.File
import java.util.Locale

import com.gomezgimenez.gcode.utils.entities.{ G, G_Motion, G_Planar_Motion, G_Unknown, Point, Segment }
import com.gomezgimenez.gcode.utils.model.SegmentsWithPower

import scala.io.Source

case class GCodeService() {
  def readGCodeFile(file: File): Vector[G] = {
    val source = Source.fromFile(file)
    val gCode =
      source
        .getLines()
        .toVector
    source.close()
    parseGCodeLines(gCode)
  }

  def parseGCodeLines(lines: Vector[String]): Vector[G] =
    fillPlanarCoordinates(lines.map { cmd =>
      G_Motion.parse(cmd).fold[G](G_Unknown(cmd))(identity)
    })

  def fillPlanarCoordinates(gCode: Vector[G]): Vector[G] =
    gCode
      .foldLeft((Vector.empty[G], 0.0, 0.0)) {
        case ((acc, lastX, lastY), n: G_Motion) if n.x.isDefined || n.y.isDefined =>
          val x = n.x.getOrElse(lastX)
          val y = n.y.getOrElse(lastY)
          (acc :+ G_Planar_Motion(n.index, x, y, n.z, n.f, n.tail), x, y)
        case ((acc, lastX, lastY), n) =>
          (acc :+ n, lastX, lastY)
      }
      ._1

  def transformGCode(gCode: Vector[G], dx: Double, dy: Double, r: Double): Vector[G] =
    gCode.map {
      case g: G_Planar_Motion =>
        val p = Point(g.x, g.y)
          .rotate(r, Point(dx, dy))
          .translate(dx, dy)
        g.copy(x = p.x, y = p.y)
      case other => other
    }

  def gCodeToSegments(gCode: Vector[G]): Vector[Segment] = {
    val gCodePoints =
      gCode.collect {
        case g: G_Planar_Motion => Point(g.x, g.y)
      }
    if (gCodePoints.size >= 2) {
      gCodePoints
        .drop(2)
        .foldLeft(Vector(Segment(gCodePoints(0), gCodePoints(1))))(
          (acc, n) => acc :+ Segment(acc.last.p2, n)
        )
    } else {
      Vector.empty
    }
  }

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
