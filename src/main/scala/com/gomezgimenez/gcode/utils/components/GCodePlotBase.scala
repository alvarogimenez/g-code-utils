package com.gomezgimenez.gcode.utils.components

import com.gomezgimenez.gcode.utils.entities.{ BoundingBox, Point, Segment }
import com.gomezgimenez.gcode.utils.model.AlignToolModel
import javafx.scene.canvas.{ Canvas, GraphicsContext }
import javafx.scene.layout.{ Border, BorderStroke, BorderStrokeStyle, Pane }
import javafx.scene.paint.Color

abstract class GCodePlotBase extends Pane {
  def draw(): Unit

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

  def drawGrid(g2d: GraphicsContext, bb: BoundingBox): Unit = {
    val b = BoundingBox(-1000, 1000, 1000, -1000)
    g2d.setLineWidth(1 / fitRatio(bb))
    g2d.setStroke(new Color(.95f, .95f, .95f, 1.0f))
    drawGridDivision(g2d, b, 1)
    g2d.setStroke(new Color(.9f, .9f, .9f, 1.0f))
    drawGridDivision(g2d, b, 10)
    g2d.setStroke(new Color(1.0f, .7f, .7f, 1.0f))
    g2d.strokeLine(0, 3, 0, -3)
    g2d.strokeLine(-3, 0, 3, 0)
    g2d.strokeOval(-2, -2, 4, 4)
  }

  def drawGridDivision(g2d: GraphicsContext, b: BoundingBox, division: Int): Unit = {
    val left   = Math.ceil(b.left / division).toInt
    val right  = Math.floor(b.right / division).toInt
    val bottom = Math.floor(b.bottom / division).toInt
    val top    = Math.ceil(b.top / division).toInt
    val countV = right - left
    val countH = top - bottom

    (0 to countH).foreach { y =>
      val p1 = Point(b.left, b.bottom + y * division)
      val p2 = Point(b.right, b.bottom + y * division)

      g2d.strokeLine(p1.x.toInt, p1.y.toInt, p2.x.toInt, p2.y.toInt)
    }

    (0 to countV).foreach { x =>
      val p1 = Point(b.left + x * division, b.top)
      val p2 = Point(b.left + x * division, b.bottom)

      g2d.strokeLine(p1.x.toInt, p1.y.toInt, p2.x.toInt, p2.y.toInt)
    }
  }

  def fitRatio(
      b: BoundingBox
  ): Double = {
    val widthRatio  = getWidth / b.width
    val heightRatio = getHeight / b.height
    Math.min(widthRatio, heightRatio)
  }

  def pointScale(
      p: Point,
      b: BoundingBox
  ): Point = {
    val ratio = fitRatio(b)

    val x1 = (p.x - b.left) / (b.right - b.left) * b.width * ratio
    val y1 = (p.y - b.bottom) / (b.top - b.bottom) * b.height * ratio
    Point(x1 + (getWidth - b.width * ratio) / 2, getHeight - y1 - (getHeight - b.height * ratio) / 2)
  }
}
