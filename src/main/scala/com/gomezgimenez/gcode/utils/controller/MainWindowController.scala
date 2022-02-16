package com.gomezgimenez.gcode.utils.controller

import com.gomezgimenez.gcode.utils.entities.Frame
import com.gomezgimenez.gcode.utils.model.GlobalModel
import com.gomezgimenez.gcode.utils.services.{ ConfigService, GCodeService }
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.Scene
import javafx.scene.control.{ Label, MenuItem }
import javafx.scene.layout.{ BorderPane, StackPane }
import javafx.scene.paint.Color
import javafx.stage.{ Modality, Stage, StageStyle }

case class MainWindowController(
    primaryStage: Stage,
    model: GlobalModel
) {

  @FXML var main_stack: StackPane       = _
  @FXML var loading_overlay: BorderPane = _
  @FXML var loading_label: Label        = _
  @FXML var menu_file_close: MenuItem   = _
  @FXML var menu_help_about: MenuItem   = _

  def initialize(): Unit = {
    loading_overlay.visibleProperty().bind(model.loading)
    loading_label.textProperty().bind(model.loadingText)

    menu_file_close.setOnAction((_: ActionEvent) => {
      primaryStage.close()
    })

    menu_help_about.setOnAction((_: ActionEvent) => {
      val stage = new Stage()

      val loader = new FXMLLoader()
      loader.setLocation(Thread.currentThread.getContextClassLoader.getResource("ui/view/About.fxml"))
      loader.setControllerFactory(_ => AboutController(stage))

      val scene = new Scene(loader.load().asInstanceOf[BorderPane])
      scene.getStylesheets.add(getClass.getResource("/ui/style/main.css").toExternalForm)
      scene.setFill(Color.TRANSPARENT)

      stage.setScene(scene)
      stage.setTitle("About")
      stage.initModality(Modality.APPLICATION_MODAL)
      stage.initStyle(StageStyle.UNDECORATED)
      stage.initStyle(StageStyle.TRANSPARENT)
      stage.show()
    })
  }

}
