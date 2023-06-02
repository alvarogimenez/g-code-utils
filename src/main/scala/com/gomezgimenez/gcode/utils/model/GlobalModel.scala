package com.gomezgimenez.gcode.utils.model

import com.gomezgimenez.gcode.utils.entities.{ GBlock, Geometry }
import javafx.beans.property.{ SimpleBooleanProperty, SimpleObjectProperty, SimpleStringProperty }

import java.io.File

case class GlobalModel() {
  val loading: SimpleBooleanProperty    = new SimpleBooleanProperty(false)
  val loadingText: SimpleStringProperty = new SimpleStringProperty()
  val lastDirectory                     = new SimpleObjectProperty[File](new File("."))

  val originalFile          = new SimpleStringProperty()
  val originalGCodeData     = new SimpleObjectProperty[Vector[GBlock]](Vector.empty)
  val originalGCodeGeometry = new SimpleObjectProperty[Vector[Geometry]](Vector.empty)
}
