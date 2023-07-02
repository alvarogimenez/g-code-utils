package com.gomezgimenez.gcode.utils.entities.geometry

case class Frame(topLeft: Point, topRight: Point, bottomLeft: Point, bottomRight: Point) {
  def segments: List[Segment] =
    List(
      Segment(topLeft, topRight),
      Segment(topLeft, bottomLeft),
      Segment(bottomLeft, bottomRight),
      Segment(bottomRight, topRight)
    )

  def points: List[Point] = List(topLeft, topRight, bottomLeft, bottomRight)

  def center: Point = GeometryUtils.centroid(points)

  def angle(f: Frame): Double = GeometryUtils.rotation(points, f.points)
}
