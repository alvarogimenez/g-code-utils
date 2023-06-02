package com.gomezgimenez.gcode.utils.entities

import java.util.Locale

import scala.util.Try

object Point {
  val PointRegex_ParStyle = """\(\s*(-?[0-9.]+)\s*,\s*(-?[0-9.]+)\s*\)""".r
  val PointRegex_RawStyle = """\s*(-?[0-9.]+)\s*,\s*(-?[0-9.]+)\s*""".r

  def fromString(s: String): Option[Point] =
    s match {
      case PointRegex_ParStyle(x, y) => Try(Point(x.toDouble, y.toDouble)).toOption
      case PointRegex_RawStyle(x, y) => Try(Point(x.toDouble, y.toDouble)).toOption
      case _                         => None
    }
}

case class Point(x: Double, y: Double) extends Geometry {
  def boundingBox: BoundingBox = BoundingBox(x,y,x,y)

  def rotate(angle: Double, center: Point = Point(0, 0)): Point = {
    val newX = center.x + (x - center.x) * Math.cos(angle) - (y - center.y) * Math
      .sin(angle)
    val newY = center.y + (x - center.x) * Math.sin(angle) + (y - center.y) * Math
      .cos(angle)
    Point(newX, newY)
  }

  def translate(_x: Double, _y: Double): Point =
    copy(x = x + _x, y = y + _y)

  def *(s: Double): Point =
    Point(x * s, y * s)

  def *(other: Point): Point =
    Point(x * other.x, y * other.y)

  def +(other: Point): Point =
    Point(x + other.x, y + other.y)

  override def toString: String =
    s"(${String.format(Locale.US, "%.3f", x)},${String.format(Locale.US, "%.3f", y)})"
}
