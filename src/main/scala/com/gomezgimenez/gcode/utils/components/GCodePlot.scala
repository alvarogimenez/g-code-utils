package com.gomezgimenez.gcode.utils.components

import com.gomezgimenez.gcode.utils.entities.{BoundingBox, Point, Segment}
import com.gomezgimenez.gcode.utils.model.DataModel
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.layout.{Border, BorderStroke, BorderStrokeStyle, Pane}
import javafx.scene.paint.Color

case class GCodePlot(model: DataModel) extends Pane {
  setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null)))

  val canvas = new Canvas(getWidth, getHeight)
  getChildren.add(canvas)
  canvas.widthProperty().addListener(_ => draw())
  canvas.heightProperty().addListener(_ => draw())

  override def layoutChildren(): Unit = {
    super.layoutChildren()
    canvas.setLayoutX(snappedLeftInset())
    canvas.setLayoutY(snappedTopInset())
    canvas.setWidth(snapSize(getWidth) - snappedLeftInset() - snappedRightInset())
    canvas.setHeight(snapSize(getHeight) - snappedTopInset() - snappedBottomInset())
  }

  model.originalFrame.addListener(_ => draw())
  model.measuredFrame.addListener(_ => draw())
  model.originalGCodeSegments.addListener(_ => draw())
  model.transposedGCodeSegments.addListener(_ => draw())

  private def draw(): Unit = {
    val g2d = canvas.getGraphicsContext2D
    g2d.setFill(Color.WHITE)
    g2d.fillRect(0, 0, getWidth, getHeight)

    val boundingBox =
      (model.originalFrame.get.map(_.segments).getOrElse(List.empty) ++
        model.measuredFrame.get.map(_.segments).getOrElse(List.empty) ++
        model.originalGCodeSegments.get ++
        model.transposedGCodeSegments.get)
        .map(_.boundingBox)
        .foldLeft(BoundingBox(0, 10, 10, 0))((a, b) => a.greater(b))
        .margin(1)

    g2d.setStroke(Color.CYAN.darker())
    model.originalFrame.get.foreach { frame =>
      frame.segments.foreach { s =>
        drawSegment(g2d, s, boundingBox)
      }
    }

    g2d.setStroke(Color.ORANGE.darker())
    model.measuredFrame.get.foreach { frame =>
      frame.segments.foreach { s =>
        drawSegment(g2d, s, boundingBox)
      }
    }

    g2d.setStroke(Color.CYAN)
    model.originalGCodeSegments.get.foreach { s =>
      drawSegment(g2d, s, boundingBox)
    }
    g2d.setStroke(Color.ORANGE)
    model.transposedGCodeSegments.get.foreach { s =>
      drawSegment(g2d, s, boundingBox)
    }
  }

  private def pointScale(
    p: Point,
    b: BoundingBox
  ): Point = {
    val widthRatio = getWidth / b.width
    val heightRatio = getHeight / b.height
    val ratio = Math.min(widthRatio, heightRatio)

    val x1 = (p.x - b.left) / (b.right - b.left) * b.width * ratio
    val y1 = (p.y - b.bottom) / (b.top - b.bottom) * b.height * ratio
    Point(x1 + (getWidth - b.width * ratio) / 2, getHeight - y1 - (getHeight - b.height * ratio) / 2)
  }

  private def drawSegment(
    g2d: GraphicsContext,
    s: Segment,
    b: BoundingBox
  ): Unit = {
    val p1 = pointScale(s.p1, b)
    val p2 = pointScale(s.p2, b)
    g2d.strokeLine(p1.x.toInt, p1.y.toInt, p2.x.toInt, p2.y.toInt)
  }
}
