package com.gomezgimenez.gcode.utils.components.editor

import com.gomezgimenez.gcode.utils.model.editor.DisplaceModel
import javafx.beans.binding.Bindings
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.{AnchorPane, BorderPane, Pane}
import javafx.util.converter.NumberStringConverter

import java.util.Locale

case class DisplaceTool(
    model: DisplaceModel,
    onDelete: Tool => Unit,
    onMoveUp: Tool => Unit,
    onMoveDown: Tool => Unit,
    onChange: Tool => Unit
) extends Tool {

  @FXML private var displace_x: TextField = _
  @FXML private var displace_y: TextField = _

  val tool = new FXMLLoader()
  tool.setLocation(Thread.currentThread.getContextClassLoader.getResource("ui/view/editor/Displace.fxml"))
  tool.setController(this)
  content.setCenter(tool.load().asInstanceOf[Pane])
  setText("Displace")
  setImage("icon/move.png")

  model.displaceX.addListener(_ => onChange(this))
  model.displaceY.addListener(_ => onChange(this))

  val c = new NumberStringConverter(Locale.ENGLISH, "#.#")

  Bindings.bindBidirectional(displace_x.textProperty(), model.displaceX, c)
  Bindings.bindBidirectional(displace_y.textProperty(), model.displaceY, c)
}
