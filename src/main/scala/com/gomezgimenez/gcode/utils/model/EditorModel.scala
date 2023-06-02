package com.gomezgimenez.gcode.utils.model

import com.gomezgimenez.gcode.utils.components.editor.Tool
import com.gomezgimenez.gcode.utils.entities._
import com.gomezgimenez.gcode.utils.model.editor.EditorToolModel
import com.gomezgimenez.gcode.utils.services.GCodeService
import javafx.application.Platform
import javafx.beans.property.{SimpleDoubleProperty, SimpleIntegerProperty, SimpleListProperty, SimpleObjectProperty}
import javafx.beans.{InvalidationListener, Observable}

import scala.concurrent.{ExecutionContext, Future}

case class EditorModel(gCodeService: GCodeService, globalModel: GlobalModel) {

  val previewData     = new SimpleObjectProperty[Vector[GBlock]](Vector.empty)
  val previewGeometry = new SimpleObjectProperty[Vector[Geometry]](Vector.empty)

  val tools = new SimpleObjectProperty[Vector[Tool]](Vector.empty)
}
