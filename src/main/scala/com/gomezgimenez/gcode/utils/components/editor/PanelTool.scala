package com.gomezgimenez.gcode.utils.components.editor

import com.gomezgimenez.gcode.utils.model.editor.{PanelModel, RotateModel}
import javafx.beans.binding.Bindings
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Spinner, SpinnerValueFactory, TextField, ToggleButton}
import javafx.scene.layout.Pane
import javafx.util.converter.NumberStringConverter

import java.util.Locale

case class PanelTool(
                      model: PanelModel,
                      onDelete: Tool => Unit,
                      onMoveUp: Tool => Unit,
                      onMoveDown: Tool => Unit,
                      onChange: Tool => Unit
                    ) extends Tool {

  @FXML private var spinner_panel_x: Spinner[Integer] = _
  @FXML private var spinner_panel_y: Spinner[Integer] = _
  @FXML private var x_align_left: ToggleButton = _
  @FXML private var x_align_middle: ToggleButton = _
  @FXML private var x_align_right: ToggleButton = _
  @FXML private var y_align_top: ToggleButton = _
  @FXML private var y_align_middle: ToggleButton = _
  @FXML private var y_align_bottom: ToggleButton = _
  @FXML private var spacing_x: TextField = _
  @FXML private var spacing_y: TextField = _

  val tool = new FXMLLoader()
  tool.setLocation(Thread.currentThread.getContextClassLoader.getResource("ui/view/editor/Panel.fxml"))
  tool.setController(this)
  content.setCenter(tool.load().asInstanceOf[Pane])
  setText("Panel")

  val c = new NumberStringConverter(Locale.ENGLISH, "#.#")

  Bindings.bindBidirectional(spacing_x.textProperty(), model.spacingX, c)
  Bindings.bindBidirectional(spacing_y.textProperty(), model.spacingY, c)

  model.panelX.addListener(_ => onChange(this))
  model.panelY.addListener(_ => onChange(this))
  model.xAlignLeft.addListener(_ => onChange(this))
  model.xAlignMiddle.addListener(_ => onChange(this))
  model.xAlignRight.addListener(_ => onChange(this))
  model.yAlignTop.addListener(_ => onChange(this))
  model.yAlignMiddle.addListener(_ => onChange(this))
  model.yAlignBottom.addListener(_ => onChange(this))
  model.spacingY.addListener(_ => onChange(this))
  model.spacingX.addListener(_ => onChange(this))

  x_align_left.selectedProperty().bindBidirectional(model.xAlignLeft)
  x_align_middle.selectedProperty().bindBidirectional(model.xAlignMiddle)
  x_align_right.selectedProperty().bindBidirectional(model.xAlignRight)
  y_align_top.selectedProperty().bindBidirectional(model.yAlignTop)
  y_align_middle.selectedProperty().bindBidirectional(model.yAlignMiddle)
  y_align_bottom.selectedProperty().bindBidirectional(model.yAlignBottom)

  spinner_panel_x.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1))
  spinner_panel_x.getValueFactory.valueProperty().bindBidirectional(model.panelXObject)

  spinner_panel_y.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1))
  spinner_panel_y.getValueFactory.valueProperty().bindBidirectional(model.panelYObject)

}
