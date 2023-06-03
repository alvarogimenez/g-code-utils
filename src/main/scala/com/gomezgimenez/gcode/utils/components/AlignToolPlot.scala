package com.gomezgimenez.gcode.utils.components

import com.gomezgimenez.gcode.utils.entities.geometry.{BoundingBox, Point, Segment}
import com.gomezgimenez.gcode.utils.model.{ AlignToolModel, GlobalModel }
import javafx.scene.canvas.{ Canvas, GraphicsContext }
import javafx.scene.layout.{ Border, BorderStroke, BorderStrokeStyle, Pane }
import javafx.scene.paint.Color
import javafx.scene.transform.Affine

case class AlignToolPlot(model: AlignToolModel, globalModel: GlobalModel) extends GCodePlotBase {
  model.originalFrame.addListener(_ => draw())
  model.measuredFrame.addListener(_ => draw())
  globalModel.editedGCodeGeometry.addListener(_ => draw())
  model.transposedGCodeGeometry.addListener(_ => draw())

  override def draw(): Unit = {
    val g2d = canvas.getGraphicsContext2D
    g2d.setTransform(new Affine())
    g2d.setFill(Color.WHITE)
    g2d.fillRect(0, 0, getWidth, getHeight)

    val boundingBox =
      (model.originalFrame.get.map(_.segments).getOrElse(List.empty) ++
      model.measuredFrame.get.map(_.segments).getOrElse(List.empty) ++
      globalModel.editedGCodeGeometry.get ++
      model.transposedGCodeGeometry.get)
        .map(_.boundingBox)
        .foldLeft(BoundingBox(0, 10, 10, 0))((a, b) => a.greater(b))
        .margin(1)

    val sp = pointScale(Point(0, 0), boundingBox)
    val fr = fitRatio(boundingBox)
    g2d.translate(sp.x, sp.y)
    g2d.scale(fr, -fr)

    drawGrid(g2d, boundingBox)

    g2d.setStroke(Color.CYAN.darker())
    model.originalFrame.get.foreach { frame =>
      frame.segments.foreach { s =>
        g2d.strokeLine(s.p1.x, s.p1.y, s.p2.x, s.p2.y)
      }
    }

    g2d.setStroke(Color.ORANGE.darker())
    model.measuredFrame.get.foreach { frame =>
      frame.segments.foreach { s =>
        g2d.strokeLine(s.p1.x, s.p1.y, s.p2.x, s.p2.y)
      }
    }

    g2d.setStroke(Color.GRAY)
    globalModel.editedGCodeGeometry.get.foreach {
      case s: Segment =>
        g2d.strokeLine(s.p1.x, s.p1.y, s.p2.x, s.p2.y)
      case p: Point =>
        g2d.strokeLine(p.x, p.y, p.x, p.y)
    }
    g2d.setStroke(Color.ORANGE)
    model.transposedGCodeGeometry.get.foreach {
      case s: Segment =>
        g2d.strokeLine(s.p1.x, s.p1.y, s.p2.x, s.p2.y)
      case p: Point =>
        g2d.strokeLine(p.x, p.y, p.x, p.y)
    }
  }
}
