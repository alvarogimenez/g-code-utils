package com.gomezgimenez.gcode.utils

import java.util.Locale

object Point {
  val PointRegex = """\(\s*([0-9.-]+)\s*,\s*([0-9.-]+)\s*\)""".r

  def fromString(s: String): Option[Point] = {
    s match {
      case PointRegex(x, y) => Some(Point(x.toDouble, y.toDouble))
      case _ => None
    }
  }
}

case class Point(x: Double, y: Double) {
  def rotate(angle: Double, center: Point = Point(0, 0)): Point = {
    val newX = center.x + (x - center.x) * Math.cos(angle) - (y - center.y) * Math
      .sin(angle)
    val newY = center.y + (x - center.x) * Math.sin(angle) + (y - center.y) * Math
      .cos(angle)
    Point(newX, newY)
  }

  def translate(_x: Double, _y: Double): Point =
    copy(x = x + _x, y = y + _y)

  override def toString: String = {
    s"(${String.format(Locale.US, "%.3f", x)},${String.format(Locale.US, "%.3f", y)})"
  }
}

case class Segment(p1: Point, p2: Point) {
  def boundingBox: BoundingBox =
    BoundingBox(
      left = Math.min(p1.x, p2.x),
      top = Math.max(p1.y, p2.y),
      right = Math.max(p1.x, p2.x),
      bottom = Math.min(p1.y, p2.y))
}

case class Frame(
  topLeft: Point,
  topRight: Point,
  bottomLeft: Point,
  bottomRight: Point) {
  def segments: List[Segment] = {
    List(
      Segment(topLeft, topRight),
      Segment(topLeft, bottomLeft),
      Segment(bottomLeft, bottomRight),
      Segment(bottomRight, topRight)
    )
  }
}

case class BoundingBox(
  left: Double,
  top: Double,
  right: Double,
  bottom: Double) {
  require(left <= right)
  require(bottom <= top)

  def greater(other: BoundingBox): BoundingBox = {
    BoundingBox(
      left = Math.min(left, other.left),
      top = Math.max(top, other.top),
      right = Math.max(right, other.right),
      bottom = Math.min(bottom, other.bottom))
  }

  def width: Double = right - left

  def height: Double = top - bottom

  def margin(m: Double): BoundingBox = {
    copy(left = left - m, right = right + m, top = top + m, bottom = bottom - m)
  }
}
