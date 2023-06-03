package com.gomezgimenez.gcode.utils.model

import com.gomezgimenez.gcode.utils.components.editor.Tool
import com.gomezgimenez.gcode.utils.services.GCodeService
import javafx.beans.property.SimpleObjectProperty

case class EditorModel(gCodeService: GCodeService, globalModel: GlobalModel) {
  val tools = new SimpleObjectProperty[Vector[Tool]](Vector.empty)
}
