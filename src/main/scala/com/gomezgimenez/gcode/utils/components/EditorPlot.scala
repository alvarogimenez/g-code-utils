package com.gomezgimenez.gcode.utils.components

import com.gomezgimenez.gcode.utils.entities.geometry.{BoundingBox, Point}
import com.gomezgimenez.gcode.utils.model.{EditorModel, GlobalModel}
import javafx.scene.paint.Color
import javafx.scene.transform.Affine

case class EditorPlot(model: EditorModel, globalModel: GlobalModel) extends GCodePlotBase {
  globalModel.originalGCodeGeometry.addListener(_ => draw())
  globalModel.editedGCodeGeometry.addListener(_ => draw())

  override def draw(): Unit = {
    val g2d = canvas.getGraphicsContext2D
    g2d.setTransform(new Affine())
    g2d.setFill(Color.WHITE)
    g2d.fillRect(0, 0, getWidth, getHeight)

    val boundingBox =
      (globalModel.originalGCodeGeometry.get ++
      globalModel.editedGCodeGeometry.get)
        .map(_.boundingBox)
        .foldLeft(BoundingBox(-10, 10, 10, -10))((a, b) => a.greater(b))
        .margin(1)

    val sp = pointScale(Point(0, 0), boundingBox)
    val fr = fitRatio(boundingBox)
    g2d.translate(sp.x, sp.y)
    g2d.scale(fr, -fr)

    drawGrid(g2d, boundingBox)

    g2d.setStroke(Color.CYAN)
    globalModel.originalGCodeGeometry.get.foreach(_.plot(g2d))
    g2d.setStroke(Color.GRAY)
    globalModel.editedGCodeGeometry.get.foreach(_.plot(g2d))
  }
}
