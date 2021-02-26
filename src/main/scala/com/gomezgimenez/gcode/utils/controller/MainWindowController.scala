package com.gomezgimenez.gcode.utils.controller

import java.io.{BufferedWriter, File, FileWriter}

import com.gomezgimenez.gcode.utils.Util
import com.gomezgimenez.gcode.utils.components.GCodePlot
import com.gomezgimenez.gcode.utils.converters.PointStringConverter
import com.gomezgimenez.gcode.utils.entities.Frame
import com.gomezgimenez.gcode.utils.model.DataModel
import com.gomezgimenez.gcode.utils.services.{ConfigService, GCodeService}
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.Scene
import javafx.scene.control.{Button, Label, MenuItem, TextField}
import javafx.scene.layout.{BorderPane, StackPane}
import javafx.scene.paint.Color
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.{Modality, Popup, Stage, StageStyle}
import javafx.util.converter.NumberStringConverter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MainWindowController(
  primaryStage: Stage,
  gCodeService: GCodeService,
  configService: ConfigService,
  model: DataModel
) {

  @FXML var main_stack: StackPane = _
  @FXML var pane_canvas: BorderPane = _
  @FXML var loading_overlay: BorderPane = _
  @FXML var loading_label: Label = _
  @FXML var menu_file_close: MenuItem = _
  @FXML var menu_file_open: MenuItem = _
  @FXML var menu_file_save_as: MenuItem = _
  @FXML var menu_help_about: MenuItem = _
  @FXML var original_frame_top_left: TextField = _
  @FXML var original_frame_top_right: TextField = _
  @FXML var original_frame_bottom_left: TextField = _
  @FXML var original_frame_bottom_right: TextField = _
  @FXML var measured_frame_top_left: TextField = _
  @FXML var measured_frame_top_right: TextField = _
  @FXML var measured_frame_bottom_left: TextField = _
  @FXML var measured_frame_bottom_right: TextField = _
  @FXML var button_transpose: Button = _
  @FXML var label_center: Label = _
  @FXML var label_rotation_std_deviation: Label = _

  def initialize(): Unit = {
    populateModelFromConfig()

    pane_canvas.setCenter(GCodePlot(model))

    loading_overlay.setVisible(false)

    original_frame_top_left.textProperty().bindBidirectional(model.originalTopLeftPoint, new PointStringConverter())
    original_frame_top_right.textProperty().bindBidirectional(model.originalTopRightPoint, new PointStringConverter())
    original_frame_bottom_left.textProperty().bindBidirectional(model.originalBottomLeftPoint, new PointStringConverter())
    original_frame_bottom_right.textProperty().bindBidirectional(model.originalBottomRightPoint, new PointStringConverter())
    measured_frame_top_left.textProperty().bindBidirectional(model.measuredTopLeftPoint, new PointStringConverter())
    measured_frame_top_right.textProperty().bindBidirectional(model.measuredTopRightPoint, new PointStringConverter())
    measured_frame_bottom_left.textProperty().bindBidirectional(model.measuredBottomLeftPoint, new PointStringConverter())
    measured_frame_bottom_right.textProperty().bindBidirectional(model.measuredBottomRightPoint, new PointStringConverter())

    label_center.textProperty().bindBidirectional(model.calculatedCenter, new PointStringConverter)
    label_rotation_std_deviation.textProperty().bindBidirectional(model.calculatedRotationStdDeviation, new NumberStringConverter())

    menu_file_open.setOnAction((_: ActionEvent) => {
      import javafx.stage.FileChooser
      val fileChooser = new FileChooser
      fileChooser.setInitialDirectory(model.lastDirectory.get)
      fileChooser.setTitle("Open G-Code File")
      fileChooser.getExtensionFilters.addAll(
        new ExtensionFilter("G-Code Files", "*.nc", "*.gcode"),
        new ExtensionFilter("All Files", "*.*"))
      val selectedFile = fileChooser.showOpenDialog(primaryStage)
      if (selectedFile != null) {
        loading_overlay.setVisible(true)
        loading_label.setText(s"""Loading file "${selectedFile.getName}"...""")
        Future {
          val gCode = gCodeService.readGCode(selectedFile)
          val gCodeSegments = gCodeService.gCodeToSegments(gCode)
          Platform.runLater(() => {
            model.lastDirectory.set(new File(selectedFile.getParent))
            model.originalFile.set(selectedFile.getAbsolutePath)
            model.originalGCodeData.set(gCode)
            model.originalGCodeSegments.set(gCodeSegments)
            model.transposedGCodeSegments.set(List.empty)
            primaryStage.setTitle(Util.windowTitle(Some(selectedFile.getName)))
            loading_overlay.setVisible(false)
          })
        }
      }
    })

    menu_file_save_as.disableProperty().bind(model.transposedGCodeData.isNotEqualTo(List.empty).not())
    menu_file_save_as.setOnAction((_: ActionEvent) => {
      import javafx.stage.FileChooser
      if (model.originalFile.get != null) {
        val file = new File(model.originalFile.get)
        val fileNameWithoutExtension = file.getName.split("\\.").dropRight(1).mkString(".")
        val fileNameExtension = file.getName.split("\\.").last
        val fileChooser = new FileChooser
        fileChooser.setInitialDirectory(file.getParentFile)
        fileChooser.setInitialFileName(fileNameWithoutExtension + "_transposed" + "." + fileNameExtension)
        fileChooser.setTitle("Save Transposed G-Code File")
        fileChooser.getExtensionFilters.addAll(
          new ExtensionFilter("G-Code Files", "*.nc", "*.gcode"),
          new ExtensionFilter("All Files", "*.*"))
        val selectedFile = fileChooser.showSaveDialog(primaryStage)
        if (selectedFile != null) {
          val gCode = model.transposedGCodeData.get
          val bw = new BufferedWriter(new FileWriter(selectedFile))
          gCode.foreach { line =>
            bw.write(line + "\n")
          }
          bw.close()
        }
      }
    })

    menu_file_close.setOnAction((_: ActionEvent) => {
      configService.saveConfiguration(configService.buildConfiguration(model))
      Platform.exit()
    })

    button_transpose.setOnAction((_: ActionEvent) => {
      loading_overlay.setVisible(true)
      loading_label.setText(s"Performing transpose...")
      model.transpose().foreach { _ =>
        Platform.runLater(() => {
          loading_overlay.setVisible(false)
        })
      }
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

  private def populateModelFromConfig(): Unit = {
    configService.loadConfiguration.foreach { config =>
      config.alignmentFrames.foreach { f =>
        model.originalTopLeftPoint.set(Some(f.originalFrame.topLeft))
        model.originalTopRightPoint.set(Some(f.originalFrame.topRight))
        model.originalBottomLeftPoint.set(Some(f.originalFrame.bottomLeft))
        model.originalBottomRightPoint.set(Some(f.originalFrame.bottomRight))
        model.originalFrame.set(Some(Frame(
          topLeft = model.originalTopLeftPoint.get.get,
          topRight = model.originalTopRightPoint.get.get,
          bottomLeft = model.originalBottomLeftPoint.get.get,
          bottomRight = model.originalBottomRightPoint.get.get
        )))
        model.measuredTopLeftPoint.set(Some(f.measuredFrame.topLeft))
        model.measuredTopRightPoint.set(Some(f.measuredFrame.topRight))
        model.measuredBottomLeftPoint.set(Some(f.measuredFrame.bottomLeft))
        model.measuredBottomRightPoint.set(Some(f.measuredFrame.bottomRight))
        model.measuredFrame.set(Some(Frame(
          topLeft = model.measuredTopLeftPoint.get.get,
          topRight = model.measuredTopRightPoint.get.get,
          bottomLeft = model.measuredBottomLeftPoint.get.get,
          bottomRight = model.measuredBottomRightPoint.get.get
        )))
      }
    }
  }
}
