package com.gomezgimenez.gcode.utils.services

import java.io.File
import java.util.Locale

import com.gomezgimenez.gcode.utils.entities.{ Point, Segment }
import com.gomezgimenez.gcode.utils.model.SegmentsWithPower

import scala.io.Source

case class GCodeService() {
  val G00_XY = """G00 X([0-9.\-]+)\s*Y([0-9.\-]+)(.*)""".r
  val G01_XY = """G01 X([0-9.\-]+)\s*Y([0-9.\-]+)(.*)""".r

  def readGCode(file: File): List[String] = {
    val source = Source.fromFile(file)
    val gCode =
      source
        .getLines()
        .toList
    source.close()
    gCode
  }

  def transformGCode(gCode: List[String], dx: Double, dy: Double, r: Double): List[String] =
    gCode
      .map {
        case G00_XY(x, y, rl) =>
          val p = Point(x.toDouble, y.toDouble)
            .rotate(r, Point(dx, dy))
            .translate(dx, dy)
          val tLine =
          s"G00 X${String.format(Locale.US, "%.3f", p.x)} " +
          s"Y${String.format(Locale.US, "%.3f", p.y)}" + rl
          tLine
        case G01_XY(x, y, rl) =>
          val p = Point(x.toDouble, y.toDouble)
            .rotate(r, Point(dx, dy))
            .translate(dx, dy)
          val tLine =
          s"G01 X${String.format(Locale.US, "%.3f", p.x)} " +
          s"Y${String.format(Locale.US, "%.3f", p.y)}" + rl
          tLine
        case line => line
      }

  def gCodeToSegments(gCode: List[String]): List[Segment] = {
    val gCodePoints =
      gCode.collect {
        case G00_XY(x, y, _) => Point(x.toDouble, y.toDouble)
        case G01_XY(x, y, _) => Point(x.toDouble, y.toDouble)
      }.toVector
    if (gCodePoints.size >= 2) {
      gCodePoints
        .drop(2)
        .foldLeft(Vector(Segment(gCodePoints(0), gCodePoints(1))))(
          (acc, n) => acc :+ Segment(acc.last.p2, n)
        )
        .toList
    } else {
      List.empty
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
