package com.gomezgimenez.gcode.utils.entities

case class Segment(p1: Point, p2: Point) {
  def boundingBox: BoundingBox =
    BoundingBox(
      left = Math.min(p1.x, p2.x),
      top = Math.max(p1.y, p2.y),
      right = Math.max(p1.x, p2.x),
      bottom = Math.min(p1.y, p2.y))
}
