package com.gomezgimenez.gcode.utils.model.editor

import javafx.beans.property.SimpleDoubleProperty

case class DisplaceModel() extends EditorToolModel {
  val displaceX: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)
  val displaceY: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)
}
