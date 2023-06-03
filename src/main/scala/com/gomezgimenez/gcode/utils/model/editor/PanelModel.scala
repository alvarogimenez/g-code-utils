package com.gomezgimenez.gcode.utils.model.editor

import javafx.beans.property.{IntegerProperty, SimpleBooleanProperty, SimpleDoubleProperty, SimpleObjectProperty}

case class PanelModel() extends EditorToolModel {
  val spacingX: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)
  val spacingY: SimpleDoubleProperty = new SimpleDoubleProperty(0.0)

  val panelXObject: SimpleObjectProperty[Integer] = new SimpleObjectProperty[Integer](1)
  val panelX: IntegerProperty = IntegerProperty.integerProperty(panelXObject)
  val panelYObject: SimpleObjectProperty[Integer] = new SimpleObjectProperty[Integer](1)
  val panelY: IntegerProperty = IntegerProperty.integerProperty(panelYObject)

  val xAlignLeft: SimpleBooleanProperty = new SimpleBooleanProperty(false)
  val xAlignMiddle: SimpleBooleanProperty = new SimpleBooleanProperty(true)
  val xAlignRight: SimpleBooleanProperty = new SimpleBooleanProperty(false)
  val yAlignTop: SimpleBooleanProperty = new SimpleBooleanProperty(false)
  val yAlignMiddle: SimpleBooleanProperty = new SimpleBooleanProperty(true)
  val yAlignBottom: SimpleBooleanProperty = new SimpleBooleanProperty(false)
}
