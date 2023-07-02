package com.gomezgimenez.gcode.utils.components.editor

import com.gomezgimenez.gcode.utils.model.editor.{MirrorModel, RotateModel}
import javafx.beans.binding.Bindings
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{TextField, ToggleButton}
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.util.converter.NumberStringConverter

import java.util.Locale

case class MirrorTool(
    model: MirrorModel,
    onDelete: Tool => Unit,
    onMoveUp: Tool => Unit,
    onMoveDown: Tool => Unit,
    onChange: Tool => Unit
) extends Tool {

  @FXML private var x_axis: TextField      = _
  @FXML private var y_axis: TextField      = _
  @FXML private var mirror_x: ToggleButton = _
  @FXML private var mirror_y: ToggleButton = _

  val tool = new FXMLLoader()
  tool.setLocation(Thread.currentThread.getContextClassLoader.getResource("ui/view/editor/Mirror.fxml"))
  tool.setController(this)
  content.setCenter(tool.load().asInstanceOf[Pane])
  setText("Mirror")
  setImage("icon/mirror-y.png")

  model.mirrorX.addListener(_ => onChange(this))
  model.mirrorY.addListener(_ => onChange(this))
  model.xAxis.addListener(_ => onChange(this))
  model.yAxis.addListener(_ => onChange(this))

  mirror_x.selectedProperty().bindBidirectional(model.mirrorX)
  mirror_y.selectedProperty().bindBidirectional(model.mirrorY)

  val c = new NumberStringConverter(Locale.ENGLISH, "#.#")

  Bindings.bindBidirectional(x_axis.textProperty(), model.xAxis, c)
  Bindings.bindBidirectional(y_axis.textProperty(), model.yAxis, c)

  x_axis.disableProperty().bind(model.mirrorX.not())
  y_axis.disableProperty().bind(model.mirrorY.not())
}
