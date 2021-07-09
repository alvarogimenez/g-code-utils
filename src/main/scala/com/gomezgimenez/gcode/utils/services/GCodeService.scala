package com.gomezgimenez.gcode.utils.services

import java.io.File
import java.util.Locale

import com.gomezgimenez.gcode.utils.entities.{Point, Segment}

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

  def transformGCode(gCode: List[String],
                     dx: Double,
                     dy: Double,
                     r: Double): List[String] = {
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
  }

  def gCodeToSegments(gCode: List[String]): List[Segment] = {
    val gCodePoints =
      gCode
        .collect {
          case G00_XY(x, y, _) => Point(x.toDouble, y.toDouble)
          case G01_XY(x, y, _) => Point(x.toDouble, y.toDouble)
        }
    gCodePoints match {
      case p1 :: p2 :: tail =>
        tail.foldLeft(List(Segment(p1, p2)))(
          (acc, n) => acc :+ Segment(acc.last.p2, n)
        )
      case _ => List.empty
    }
  }
}
