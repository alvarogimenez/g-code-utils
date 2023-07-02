package com.gomezgimenez.gcode.utils.model

import com.gomezgimenez.gcode.utils.entities.GBlock
import com.gomezgimenez.gcode.utils.entities.geometry.Geometry
import javafx.beans.property.{SimpleBooleanProperty, SimpleObjectProperty, SimpleStringProperty}

import java.io.File

case class GlobalModel() {
  val loading: SimpleBooleanProperty    = new SimpleBooleanProperty(false)
  val loadingText: SimpleStringProperty = new SimpleStringProperty()
  val lastDirectory                     = new SimpleObjectProperty[File](new File("."))

  val originalFile          = new SimpleStringProperty()
  val originalGCodeData     = new SimpleObjectProperty[Vector[GBlock]](Vector.empty)
  val originalGCodeGeometry     = new SimpleObjectProperty[Vector[Geometry]](Vector.empty)
  val editedGCodeData     = new SimpleObjectProperty[Vector[GBlock]](Vector.empty)
  val editedGCodeGeometry = new SimpleObjectProperty[Vector[Geometry]](Vector.empty)
}
