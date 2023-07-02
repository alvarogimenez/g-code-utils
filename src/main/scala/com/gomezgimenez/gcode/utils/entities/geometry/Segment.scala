package com.gomezgimenez.gcode.utils.entities.geometry
import javafx.scene.canvas.GraphicsContext

case class Segment(p1: Point, p2: Point, dotted: Boolean = false) extends Geometry {
  def boundingBox: BoundingBox =
    BoundingBox(left = Math.min(p1.x, p2.x), top = Math.max(p1.y, p2.y), right = Math.max(p1.x, p2.x), bottom = Math.min(p1.y, p2.y))

  override def plot(g2d: GraphicsContext): Unit = {
    g2d.save()
    if(dotted) {
      g2d.setLineDashes(0.5)
    }
    g2d.strokeLine(p1.x, p1.y, p2.x, p2.y)
    g2d.restore()
  }

  def *(s: Double) = Segment(p1 * s, p2 * s)
  def *(p: Point)  = Segment(p1 * p, p2 * p)
  def +(p: Point)  = Segment(p1 + p, p2 + p)
}
