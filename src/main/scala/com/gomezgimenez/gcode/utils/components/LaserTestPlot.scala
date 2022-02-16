package com.gomezgimenez.gcode.utils.components

import com.gomezgimenez.gcode.utils.entities.{ BoundingBox, Point }
import com.gomezgimenez.gcode.utils.model.LaserTestToolModel
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap
import javafx.scene.transform.Affine

case class LaserTestPlot(model: LaserTestToolModel) extends GCodePlotBase {
  model.segments.addListener(_ => draw())

  override def draw(): Unit = {
    val g2d = canvas.getGraphicsContext2D
    g2d.setTransform(new Affine())
    g2d.setFill(Color.WHITE)
    g2d.fillRect(0, 0, getWidth, getHeight)
    g2d.setLineCap(StrokeLineCap.ROUND)

    val segments = model.segments.get.flatMap(_.segments)

    val boundingBox = segments
      .map(_.boundingBox)
      .foldLeft(BoundingBox(0, 0, 0, 0))((a, b) => a.greater(b))
      .margin(1)

    val sp = pointScale(Point(0, 0), boundingBox)
    val fr = fitRatio(boundingBox)
    g2d.translate(sp.x, sp.y)
    g2d.scale(fr, -fr)

    drawGrid(g2d, boundingBox)

    g2d.setStroke(Color.CYAN.darker())
    g2d.setLineWidth(0.15)
    segments.foreach { s =>
      g2d.strokeLine(s.p1.x, s.p1.y, s.p2.x, s.p2.y)
    }
  }
}
