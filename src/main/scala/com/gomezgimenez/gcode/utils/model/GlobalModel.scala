package com.gomezgimenez.gcode.utils.model

import javafx.beans.property.{ SimpleBooleanProperty, SimpleStringProperty }

case class GlobalModel() {
  val loading: SimpleBooleanProperty    = new SimpleBooleanProperty(false)
  val loadingText: SimpleStringProperty = new SimpleStringProperty()
}
