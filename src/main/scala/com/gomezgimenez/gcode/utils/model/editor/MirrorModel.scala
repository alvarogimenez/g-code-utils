package com.gomezgimenez.gcode.utils.model.editor

import javafx.beans.property.{SimpleBooleanProperty, SimpleDoubleProperty}

case class MirrorModel() extends EditorToolModel {
  val xAxis: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)
  val yAxis: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)
  val mirrorX: SimpleBooleanProperty = new SimpleBooleanProperty(false)
  val mirrorY: SimpleBooleanProperty = new SimpleBooleanProperty(false)
}
