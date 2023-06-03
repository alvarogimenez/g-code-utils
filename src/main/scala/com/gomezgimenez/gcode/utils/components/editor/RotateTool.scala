package com.gomezgimenez.gcode.utils.components.editor

import com.gomezgimenez.gcode.utils.model.editor.{DisplaceModel, RotateModel}
import javafx.beans.binding.Bindings
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.util.converter.NumberStringConverter

import java.util.Locale

case class RotateTool(
    model: RotateModel,
    onDelete: Tool => Unit,
    onMoveUp: Tool => Unit,
    onMoveDown: Tool => Unit,
    onChange: Tool => Unit
) extends Tool {

  @FXML private var center_x: TextField = _
  @FXML private var center_y: TextField = _
  @FXML private var angle: TextField    = _

  val tool = new FXMLLoader()
  tool.setLocation(Thread.currentThread.getContextClassLoader.getResource("ui/view/editor/Rotate.fxml"))
  tool.setController(this)
  content.setCenter(tool.load().asInstanceOf[Pane])
  setText("Rotate")
  tool_image.setImage(new Image("icon/rotate.png", 15, 15, true, true))

  model.centerX.addListener(_ => onChange(this))
  model.centerY.addListener(_ => onChange(this))
  model.angle.addListener(_ => onChange(this))

  val c = new NumberStringConverter(Locale.ENGLISH, "#.#")

  Bindings.bindBidirectional(center_x.textProperty(), model.centerX, c)
  Bindings.bindBidirectional(center_y.textProperty(), model.centerY, c)
  Bindings.bindBidirectional(angle.textProperty(), model.angle, c)
}
