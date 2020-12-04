package com.gomezgimenez.gcode.utils.entities

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
