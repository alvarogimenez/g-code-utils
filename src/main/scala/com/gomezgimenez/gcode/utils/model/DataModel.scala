package com.gomezgimenez.gcode.utils.model

import com.gomezgimenez.gcode.utils.services.GCode
import com.gomezgimenez.gcode.utils.{Frame, Point, Segment}
import javafx.beans.property.{SimpleDoubleProperty, SimpleObjectProperty, SimpleStringProperty}
import javafx.beans.{InvalidationListener, Observable}

case class DataModel() {
  val originalTopLeftPoint = new SimpleObjectProperty[Option[Point]](Some(Point(0,0)))
  val originalTopRightPoint = new SimpleObjectProperty[Option[Point]](Some(Point(0,0)))
  val originalBottomLeftPoint = new SimpleObjectProperty[Option[Point]](Some(Point(0,0)))
  val originalBottomRightPoint = new SimpleObjectProperty[Option[Point]](Some(Point(0,0)))

  val measuredTopLeftPoint = new SimpleObjectProperty[Option[Point]](Some(Point(0,0)))
  val measuredTopRightPoint = new SimpleObjectProperty[Option[Point]](Some(Point(0,0)))
  val measuredBottomLeftPoint = new SimpleObjectProperty[Option[Point]](Some(Point(0,0)))
  val measuredBottomRightPoint = new SimpleObjectProperty[Option[Point]](Some(Point(0,0)))

  val calculatedCenter = new SimpleObjectProperty[Option[Point]](None)
  val calculatedRotationStdDeviation = new SimpleDoubleProperty()

  val originalFrame = new SimpleObjectProperty[Option[Frame]](None)
  val measuredFrame = new SimpleObjectProperty[Option[Frame]](None)
  val originalFile = new SimpleStringProperty()
  val originalGCodeData = new SimpleObjectProperty[List[String]](List.empty)
  val originalGCodeSegments = new SimpleObjectProperty[List[Segment]](List.empty)
  val transposedGCodeData = new SimpleObjectProperty[List[String]](List.empty)
  val transposedGCodeSegments = new SimpleObjectProperty[List[Segment]](List.empty)

  private val buildFramesListener = new InvalidationListener {
    override def invalidated(observable: Observable): Unit = buildFrames()
  }
  originalTopLeftPoint.addListener(buildFramesListener)
  originalTopRightPoint.addListener(buildFramesListener)
  originalBottomLeftPoint.addListener(buildFramesListener)
  originalBottomRightPoint.addListener(buildFramesListener)
  measuredTopLeftPoint.addListener(buildFramesListener)
  measuredTopRightPoint.addListener(buildFramesListener)
  measuredBottomLeftPoint.addListener(buildFramesListener)
  measuredBottomRightPoint.addListener(buildFramesListener)

  def buildFrames(): Unit = {
    originalFrame.set(for {
      topLeft <- originalTopLeftPoint.get
      topRight <- originalTopRightPoint.get
      bottomLeft <- originalBottomLeftPoint.get
      bottomRight <- originalBottomRightPoint.get
    } yield {
      Frame(topLeft, topRight, bottomLeft, bottomRight)
    })
    measuredFrame.set(for {
      topLeft <- measuredTopLeftPoint.get
      topRight <- measuredTopRightPoint.get
      bottomLeft <- measuredBottomLeftPoint.get
      bottomRight <- measuredBottomRightPoint.get
    } yield {
      Frame(topLeft, topRight, bottomLeft, bottomRight)
    })
    calculatedCenter.set(avgCenter)
    calculatedRotationStdDeviation.set(avgRotation.getOrElse(0.0))
  }

  def transpose(): Unit = {
    val (_transposedGCodeData, _transposedGCodeSegments) =
      (for {
      avgCenter <- avgCenter
      avgRotation <- avgRotation
    } yield {
      val gCode = GCode.transformGCode(originalGCodeData.get, avgCenter.x, avgCenter.y, avgRotation)
        (gCode, GCode.gCodeToSegments(gCode))
    }).getOrElse((List.empty[String], List.empty[Segment]))
    transposedGCodeData.set(_transposedGCodeData)
    transposedGCodeSegments.set(_transposedGCodeSegments)
  }

  def avgCenter: Option[Point] = {
    for {
      measuredFrame <- measuredFrame.get
    } yield {
      val x1 = measuredFrame.topLeft.x
      val y1 = measuredFrame.topLeft.y
      val x2 = measuredFrame.bottomRight.x
      val y2 = measuredFrame.bottomRight.y
      val x3 = measuredFrame.topRight.x
      val y3 = measuredFrame.topRight.y
      val x4 = measuredFrame.bottomLeft.x
      val y4 = measuredFrame.bottomLeft.y
      val x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4))
      val y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4))
      Point(x, y)
    }
  }

  def avgRotation: Option[Double] = {
    for {
      measuredFrame <- measuredFrame.get
      originalFrame <- originalFrame.get
    } yield {
      val original_x1 = originalFrame.topLeft.x
      val original_y1 = originalFrame.topLeft.y
      val original_x2 = originalFrame.bottomRight.x
      val original_y2 = originalFrame.bottomRight.y
      val original_x3 = originalFrame.topRight.x
      val original_y3 = originalFrame.topRight.y
      val original_x4 = originalFrame.bottomLeft.x
      val original_y4 = originalFrame.bottomLeft.y
      val original_r1 = Math.atan2(original_y1, original_x1)
      val original_r2 = Math.atan2(original_y2, original_x2)
      val original_r3 = Math.atan2(original_y3, original_x3)
      val original_r4 = Math.atan2(original_y4, original_x4)
      val measured_x1 = measuredFrame.topLeft.x
      val measured_y1 = measuredFrame.topLeft.y
      val measured_x2 = measuredFrame.bottomRight.x
      val measured_y2 = measuredFrame.bottomRight.y
      val measured_x3 = measuredFrame.topRight.x
      val measured_y3 = measuredFrame.topRight.y
      val measured_x4 = measuredFrame.bottomLeft.x
      val measured_y4 = measuredFrame.bottomLeft.y
      val measured_r1 = Math.atan2(measured_y1, measured_x1)
      val measured_r2 = Math.atan2(measured_y2, measured_x2)
      val measured_r3 = Math.atan2(measured_y3, measured_x3)
      val measured_r4 = Math.atan2(measured_y4, measured_x4)
      ((measured_r1 - original_r1) +
        (measured_r2 - original_r2) +
        (measured_r3 - original_r3) +
        (measured_r4 - original_r4)) / 4.0
    }
  }
}
