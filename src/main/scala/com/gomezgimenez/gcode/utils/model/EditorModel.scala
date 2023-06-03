package com.gomezgimenez.gcode.utils.model

import com.gomezgimenez.gcode.utils.components.editor.Tool
import com.gomezgimenez.gcode.utils.services.GCodeService
import javafx.beans.property.{SimpleListProperty, SimpleObjectProperty}
import javafx.collections.{FXCollections, ObservableList}

case class EditorModel(gCodeService: GCodeService, globalModel: GlobalModel) {
  val tools = new SimpleObjectProperty[Vector[Tool]](Vector.empty)
  val gCodeList: ObservableList[String] = FXCollections.observableArrayList()
  val gCode: SimpleListProperty[String] = new SimpleListProperty[String](gCodeList)
}
