package com.gomezgimenez.gcode.utils.controller

import java.io.{ BufferedWriter, File, FileWriter }

import com.gomezgimenez.gcode.utils.Util
import com.gomezgimenez.gcode.utils.components.AlignToolPlot
import com.gomezgimenez.gcode.utils.converters.PointStringConverter
import com.gomezgimenez.gcode.utils.entities.Frame
import com.gomezgimenez.gcode.utils.model.{ AlignToolModel, GlobalModel }
import com.gomezgimenez.gcode.utils.services.{ ConfigService, GCodeService }
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.Scene
import javafx.scene.control.{ Button, Label, MenuItem, TextField }
import javafx.scene.layout.{ BorderPane, StackPane }
import javafx.scene.paint.Color
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.{ Modality, Stage, StageStyle }
import javafx.util.converter.NumberStringConverter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class AlignToolTabController(
    primaryStage: Stage,
    gCodeService: GCodeService,
    globalModel: GlobalModel,
    model: AlignToolModel
) {

  @FXML var alignment_tool_canvas: BorderPane = _

  @FXML var original_frame_top_left: TextField     = _
  @FXML var original_frame_top_right: TextField    = _
  @FXML var original_frame_bottom_left: TextField  = _
  @FXML var original_frame_bottom_right: TextField = _
  @FXML var measured_frame_top_left: TextField     = _
  @FXML var measured_frame_top_right: TextField    = _
  @FXML var measured_frame_bottom_left: TextField  = _
  @FXML var measured_frame_bottom_right: TextField = _

  @FXML var button_open: Button      = _
  @FXML var button_save_as: Button   = _
  @FXML var button_transpose: Button = _
  @FXML var label_center: Label      = _

  @FXML var label_rotation_std_deviation: Label = _

  def initialize(): Unit = {
    alignment_tool_canvas.setCenter(AlignToolPlot(model))

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

    button_open.setOnAction((_: ActionEvent) => {
      import javafx.stage.FileChooser
      val fileChooser = new FileChooser
      fileChooser.setInitialDirectory(model.lastDirectory.get)
      fileChooser.setTitle("Open G-Code File")
      fileChooser.getExtensionFilters.addAll(new ExtensionFilter("G-Code Files", "*.nc", "*.gcode"), new ExtensionFilter("All Files", "*.*"))
      val selectedFile = fileChooser.showOpenDialog(primaryStage)
      if (selectedFile != null) {
        globalModel.loading.set(true)
        globalModel.loadingText.set(s"""Loading file "${selectedFile.getName}"...""")
        Future {
          val gCode         = gCodeService.readGCode(selectedFile)
          val gCodeSegments = gCodeService.gCodeToSegments(gCode)
          Platform.runLater(() => {
            model.lastDirectory.set(new File(selectedFile.getParent))
            model.originalFile.set(selectedFile.getAbsolutePath)
            model.originalGCodeData.set(gCode)
            model.originalGCodeSegments.set(gCodeSegments)
            model.transposedGCodeSegments.set(List.empty)
            primaryStage.setTitle(Util.windowTitle(Some(selectedFile.getName)))
            globalModel.loading.set(false)
          })
        }
      }
    })

    button_save_as.disableProperty().bind(model.transposedGCodeData.isNotEqualTo(List.empty).not())
    button_save_as.setOnAction((_: ActionEvent) => {
      import javafx.stage.FileChooser
      if (model.originalFile.get != null) {
        val file                     = new File(model.originalFile.get)
        val fileNameWithoutExtension = file.getName.split("\\.").dropRight(1).mkString(".")
        val fileNameExtension        = file.getName.split("\\.").last
        val fileChooser              = new FileChooser
        fileChooser.setInitialDirectory(file.getParentFile)
        fileChooser.setInitialFileName(fileNameWithoutExtension + "_transposed" + "." + fileNameExtension)
        fileChooser.setTitle("Save Transposed G-Code File")
        fileChooser.getExtensionFilters.addAll(new ExtensionFilter("G-Code Files", "*.nc", "*.gcode"), new ExtensionFilter("All Files", "*.*"))
        val selectedFile = fileChooser.showSaveDialog(primaryStage)
        if (selectedFile != null) {
          val gCode = model.transposedGCodeData.get
          val bw    = new BufferedWriter(new FileWriter(selectedFile))
          gCode.foreach { line =>
            bw.write(line + "\n")
          }
          bw.close()
        }
      }
    })

    button_transpose.setOnAction((_: ActionEvent) => {
      globalModel.loading.set(true)
      globalModel.loadingText.set(s"Performing transpose...")
      model.transpose().foreach { _ =>
        Platform.runLater(() => {
          globalModel.loading.set(false)
        })
      }
    })
  }
}
