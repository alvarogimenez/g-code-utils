package com.gomezgimenez.gcode.utils.components.editor

import javafx.event.ActionEvent
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.Node
import javafx.scene.control.{Button, TitledPane}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.BorderPane

trait Tool extends TitledPane {
  def onDelete: Tool => Unit
  def onMoveUp: Tool => Unit
  def onMoveDown: Tool => Unit
  def onChange: Tool => Unit

  @FXML var move_up: Button = _
  @FXML var move_down: Button = _
  @FXML var delete: Button = _
  @FXML var content: BorderPane = _
  @FXML var tool_image: ImageView = _

  val wrapper = new FXMLLoader()
  wrapper.setLocation(Thread.currentThread.getContextClassLoader.getResource("ui/view/editor/Wrapper.fxml"))
  wrapper.setController(this)
  wrapper.setRoot(this)
  wrapper.load()
  initializeWrapper()

  def initializeWrapper(): Unit = {
    delete.setOnAction((_: ActionEvent) => onDelete(this))
    move_up.setOnAction((_: ActionEvent) => onMoveUp(this))
    move_down.setOnAction((_: ActionEvent) => onMoveDown(this))
  }

  def setImage(url: String): Unit = {
    tool_image.setImage(new Image(url, 15, 15, true, true))
  }
}
