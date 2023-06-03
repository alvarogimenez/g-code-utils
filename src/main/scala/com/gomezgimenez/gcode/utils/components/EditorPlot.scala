package com.gomezgimenez.gcode.utils.components

import com.gomezgimenez.gcode.utils.entities.geometry.{BoundingBox, Point, Segment}
import com.gomezgimenez.gcode.utils.model.{AlignToolModel, EditorModel, GlobalModel}
import javafx.scene.paint.Color
import javafx.scene.transform.Affine

case class EditorPlot(model: EditorModel, globalModel: GlobalModel) extends GCodePlotBase {
  globalModel.originalGCodeGeometry.addListener(_ => draw())
  model.previewGeometry.addListener(_ => draw())

  override def draw(): Unit = {
    val g2d = canvas.getGraphicsContext2D
    g2d.setTransform(new Affine())
    g2d.setFill(Color.WHITE)
    g2d.fillRect(0, 0, getWidth, getHeight)

    val boundingBox =
      (globalModel.originalGCodeGeometry.get ++
      model.previewGeometry.get)
        .map(_.boundingBox)
        .foldLeft(BoundingBox(0, 10, 10, 0))((a, b) => a.greater(b))
        .margin(1)

    val sp = pointScale(Point(0, 0), boundingBox)
    val fr = fitRatio(boundingBox)
    g2d.translate(sp.x, sp.y)
    g2d.scale(fr, -fr)

    drawGrid(g2d, boundingBox)

    g2d.setStroke(Color.CYAN)
    globalModel.originalGCodeGeometry.get.foreach {
      case s: Segment =>
        g2d.strokeLine(s.p1.x, s.p1.y, s.p2.x, s.p2.y)
      case p: Point =>
        g2d.strokeLine(p.x, p.y, p.x, p.y)
    }
    g2d.setStroke(Color.GRAY)
    model.previewGeometry.get.foreach {
      case s: Segment =>
        g2d.strokeLine(s.p1.x, s.p1.y, s.p2.x, s.p2.y)
      case p: Point =>
        g2d.strokeLine(p.x, p.y, p.x, p.y)
    }
  }
}
