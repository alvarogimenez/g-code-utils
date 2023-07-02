package com.gomezgimenez.gcode.utils.entities.geometry

import javafx.scene.canvas.GraphicsContext
import javafx.scene.shape.ArcType

case class Arc(center: Point, radius: Double, startAngle: Double, extent: Double, arcType: Int) extends Geometry {
  def boundingBox: BoundingBox =
    BoundingBox(
      left = center.x - radius,
      top = center.y + radius,
      right = center.x + radius,
      bottom = center.y - radius
    )

  override def plot(g2d: GraphicsContext): Unit = {
    g2d.beginPath()
    g2d.strokeArc(center.x - radius, center.y - radius, radius * 2, radius * 2, startAngle, extent, ArcType.OPEN)
    g2d.stroke()
  }


}
