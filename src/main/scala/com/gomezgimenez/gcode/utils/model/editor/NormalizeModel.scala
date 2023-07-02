package com.gomezgimenez.gcode.utils.model.editor

import javafx.beans.property.SimpleBooleanProperty

case class NormalizeModel() extends EditorToolModel {
  val normalizeCommands = new SimpleBooleanProperty(true)
  val normalizeCoordinates = new SimpleBooleanProperty(true)
}
