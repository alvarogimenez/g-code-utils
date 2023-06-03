package com.gomezgimenez.gcode.utils.model

import com.gomezgimenez.gcode.utils.entities.geometry.{Frame, Geometry, Point, Segment}
import java.io.File
import com.gomezgimenez.gcode.utils.entities.GBlock
import com.gomezgimenez.gcode.utils.services.GCodeService
import javafx.application.Platform
import javafx.beans.property.{SimpleBooleanProperty, SimpleDoubleProperty, SimpleObjectProperty, SimpleStringProperty}
import javafx.beans.{InvalidationListener, Observable}

import scala.concurrent.{ExecutionContext, Future}

case class AlignToolModel(gCodeService: GCodeService, globalModel: GlobalModel) {

  val originalTopLeftPoint =
    new SimpleObjectProperty[Option[Point]](Some(Point(0, 0)))
  val originalTopRightPoint =
    new SimpleObjectProperty[Option[Point]](Some(Point(0, 0)))
  val originalBottomLeftPoint =
    new SimpleObjectProperty[Option[Point]](Some(Point(0, 0)))
  val originalBottomRightPoint =
    new SimpleObjectProperty[Option[Point]](Some(Point(0, 0)))

  val measuredTopLeftPoint =
    new SimpleObjectProperty[Option[Point]](Some(Point(0, 0)))
  val measuredTopRightPoint =
    new SimpleObjectProperty[Option[Point]](Some(Point(0, 0)))
  val measuredBottomLeftPoint =
    new SimpleObjectProperty[Option[Point]](Some(Point(0, 0)))
  val measuredBottomRightPoint =
    new SimpleObjectProperty[Option[Point]](Some(Point(0, 0)))

  val calculatedCenter               = new SimpleObjectProperty[Option[Point]](None)
  val calculatedRotation             = new SimpleDoubleProperty()

  val originalFrame     = new SimpleObjectProperty[Option[Frame]](None)
  val measuredFrame     = new SimpleObjectProperty[Option[Frame]](None)
  val transposedGCodeData = new SimpleObjectProperty[Vector[GBlock]](Vector.empty)
  val transposedGCodeGeometry =
    new SimpleObjectProperty[Vector[Geometry]](Vector.empty)

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
      topLeft     <- originalTopLeftPoint.get
      topRight    <- originalTopRightPoint.get
      bottomLeft  <- originalBottomLeftPoint.get
      bottomRight <- originalBottomRightPoint.get
    } yield {
      Frame(topLeft, topRight, bottomLeft, bottomRight)
    })
    measuredFrame.set(for {
      topLeft     <- measuredTopLeftPoint.get
      topRight    <- measuredTopRightPoint.get
      bottomLeft  <- measuredBottomLeftPoint.get
      bottomRight <- measuredBottomRightPoint.get
    } yield {
      Frame(topLeft, topRight, bottomLeft, bottomRight)
    })
    calculatedCenter.set(measuredFrame.get().map(_.center))
    calculatedRotation.set(
      rotation().getOrElse(0.0)
    )
  }

  private def rotation(): Option[Double] = {
    for {
      originalFrame <- originalFrame.get()
      measuredFrame <- measuredFrame.get()
    } yield originalFrame.angle(measuredFrame)
  }

  def transpose()(implicit ec: ExecutionContext): Future[Unit] =
    Future {
      val (_transposedGCodeData, _transposedGCodeSegments) =
        (for {
          origCenter  <- originalFrame.get().map(_.center)
          avgCenter   <- measuredFrame.get().map(_.center)
          avgRotation <- rotation()
        } yield {
          val gCode = gCodeService.rotateAndDisplace(
            gCode = globalModel.originalGCodeData.get,
            dx = avgCenter.x - origCenter.x,
            dy = avgCenter.y - origCenter.y,
            cx = origCenter.x,
            cy = origCenter.y,
            r = Math.toRadians(avgRotation)
          )
          (gCode, gCodeService.gCodeToSegments(gCode))
        }).getOrElse((Vector.empty[GBlock], Vector.empty[Segment]))

      Platform.runLater(() => {
        transposedGCodeData.set(_transposedGCodeData)
        transposedGCodeGeometry.set(_transposedGCodeSegments)
      })
    }
}
