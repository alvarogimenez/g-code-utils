package com.gomezgimenez.gcode.utils.model

import com.gomezgimenez.gcode.utils.components.editor.Tool
import com.gomezgimenez.gcode.utils.entities._
import com.gomezgimenez.gcode.utils.entities.geometry.Geometry
import com.gomezgimenez.gcode.utils.services.GCodeService
import javafx.beans.property.SimpleObjectProperty

case class EditorModel(gCodeService: GCodeService, globalModel: GlobalModel) {

  val previewData     = new SimpleObjectProperty[Vector[GBlock]](Vector.empty)
  val previewGeometry = new SimpleObjectProperty[Vector[Geometry]](Vector.empty)
  val tools = new SimpleObjectProperty[Vector[Tool]](Vector.empty)
}
