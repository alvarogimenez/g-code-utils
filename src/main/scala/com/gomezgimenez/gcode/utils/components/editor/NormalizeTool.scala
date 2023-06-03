package com.gomezgimenez.gcode.utils.components.editor

import com.gomezgimenez.gcode.utils.model.editor.{DisplaceModel, NormalizeModel}
import javafx.beans.binding.Bindings
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{CheckBox, TextField}
import javafx.scene.layout.Pane
import javafx.util.converter.NumberStringConverter

import java.util.Locale

case class NormalizeTool(
    model: NormalizeModel,
    onDelete: Tool => Unit,
    onMoveUp: Tool => Unit,
    onMoveDown: Tool => Unit,
    onChange: Tool => Unit
) extends Tool {

  @FXML private var checkbox_commands: CheckBox = _
  @FXML private var checkbox_coordinates: CheckBox = _

  val tool = new FXMLLoader()
  tool.setLocation(Thread.currentThread.getContextClassLoader.getResource("ui/view/editor/Normalize.fxml"))
  tool.setController(this)
  content.setCenter(tool.load().asInstanceOf[Pane])
  setText("Normalize")
  setImage("icon/normalize.png")

  model.normalizeCommands.addListener(_ => onChange(this))
  model.normalizeCoordinates.addListener(_ => onChange(this))

  checkbox_commands.selectedProperty().bindBidirectional(model.normalizeCommands)
  checkbox_coordinates.selectedProperty().bindBidirectional(model.normalizeCoordinates)
}
