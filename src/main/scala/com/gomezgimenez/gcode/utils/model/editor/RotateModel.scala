package com.gomezgimenez.gcode.utils.model.editor

import javafx.beans.property.SimpleDoubleProperty

case class RotateModel() extends EditorToolModel {
  val centerX: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)
  val centerY: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)
  val angle: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)
}
