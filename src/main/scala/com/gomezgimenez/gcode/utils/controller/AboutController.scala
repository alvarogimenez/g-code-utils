package com.gomezgimenez.gcode.utils.controller

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.stage.Stage

case class AboutController(stage: Stage) {
  @FXML var close_button: Button = _

  def initialize(): Unit = {
    close_button.setOnAction(_ => {
      println("Close")
      stage.hide()
    })
  }
}
