package com.gomezgimenez.gcode.utils.entities

case class Segment(p1: Point, p2: Point) extends Geometry {
  def boundingBox: BoundingBox =
    BoundingBox(left = Math.min(p1.x, p2.x), top = Math.max(p1.y, p2.y), right = Math.max(p1.x, p2.x), bottom = Math.min(p1.y, p2.y))

  def *(s: Double) = Segment(p1 * s, p2 * s)
  def *(p: Point)  = Segment(p1 * p, p2 * p)
  def +(p: Point)  = Segment(p1 + p, p2 + p)
}
