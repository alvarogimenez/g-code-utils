package com.gomezgimenez.gcode.utils.model.editor

import com.gomezgimenez.gcode.utils.entities._
import com.gomezgimenez.gcode.utils.services.GCodeService
import javafx.beans.property.{SimpleDoubleProperty, SimpleObjectProperty}

case class DisplaceModel() extends EditorToolModel {
  val displaceX: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)
  val displaceY: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)
  val rotate: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)

}
