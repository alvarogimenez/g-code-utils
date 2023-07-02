package com.gomezgimenez.gcode.utils.entities.geometry

case class BoundingBox(left: Double, top: Double, right: Double, bottom: Double) {
  require(left <= right)
  require(bottom <= top)

  def greater(other: BoundingBox): BoundingBox =
    BoundingBox(left = Math.min(left, other.left), top = Math.max(top, other.top), right = Math.max(right, other.right), bottom = Math.min(bottom, other.bottom))

  def width: Double = right - left

  def height: Double = top - bottom

  def center: Point = Point(left + width / 2, bottom + height / 2)

  def margin(m: Double): BoundingBox =
    copy(left = left - m, right = right + m, top = top + m, bottom = bottom - m)
}
