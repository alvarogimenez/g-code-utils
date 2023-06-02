package com.gomezgimenez.gcode.utils.controller

import com.gomezgimenez.gcode.utils.components.EditorPlot
import com.gomezgimenez.gcode.utils.components.editor.{DisplaceTool, Tool}
import com.gomezgimenez.gcode.utils.model.editor.DisplaceModel
import com.gomezgimenez.gcode.utils.model.{EditorModel, GlobalModel}
import com.gomezgimenez.gcode.utils.services.GCodeService
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Button, MenuItem}
import javafx.scene.layout.{BorderPane, VBox}
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage

import java.io.{BufferedWriter, File, FileWriter}
import scala.jdk.CollectionConverters.CollectionHasAsScala

case class EditorTabController(
    primaryStage: Stage,
    gCodeService: GCodeService,
    globalModel: GlobalModel,
    model: EditorModel
) {

  @FXML private var tools: VBox = _
  @FXML private var editor_canvas: BorderPane = _

  @FXML private var button_open: Button      = _
  @FXML private var button_save_as: Button   = _

  @FXML private var add_tool_displace: MenuItem   = _
  @FXML private var add_tool_panel: MenuItem   = _

  def initialize(): Unit = {
    editor_canvas.setCenter(EditorPlot(model, globalModel))

    add_tool_displace.setOnAction((_: ActionEvent) => {
      tools.getChildren.add(DisplaceTool(
        model = DisplaceModel(),
        onDelete = onToolDelete,
        onMoveUp = onToolMoveUp,
        onMoveDown = onToolMoveDown,
        onChange = onToolChange
      ))
    })

    add_tool_panel.setOnAction((_: ActionEvent) => {
      println("Add panel tool")
    })

    button_open.setOnAction((_: ActionEvent) => {
      load(primaryStage, globalModel, gCodeService, () => {
        recalculatePreview()
      })
    })

    button_save_as.disableProperty().bind(model.previewData.isNotEqualTo(Vector.empty).not())
    button_save_as.setOnAction((_: ActionEvent) => {
      import javafx.stage.FileChooser
      if (globalModel.originalFile.get != null) {
        val file                     = new File(globalModel.originalFile.get)
        val fileNameWithoutExtension = file.getName.split("\\.").dropRight(1).mkString(".")
        val fileNameExtension        = file.getName.split("\\.").last
        val fileChooser              = new FileChooser
        fileChooser.setInitialDirectory(file.getParentFile)
        fileChooser.setInitialFileName(fileNameWithoutExtension + "_transposed" + "." + fileNameExtension)
        fileChooser.setTitle("Save Transposed G-Code File")
        fileChooser.getExtensionFilters.addAll(new ExtensionFilter("G-Code Files", "*.nc", "*.gcode"), new ExtensionFilter("All Files", "*.*"))
        val selectedFile = fileChooser.showSaveDialog(primaryStage)
        if (selectedFile != null) {
          val gCode = model.previewData.get
          val bw    = new BufferedWriter(new FileWriter(selectedFile))
          gCode.foreach { line =>
            bw.write(line.print + "\n")
          }
          bw.close()
        }
      }
    })
  }

  private def onToolDelete(t: Tool): Unit = {
    tools.getChildren.remove(t)
    recalculatePreview()
  }

  private def onToolMoveUp(t: Tool): Unit = {
    val index = tools.getChildren.indexOf(t)
    if (index > 0) {
      tools.getChildren.remove(t)
      tools.getChildren.add(index - 1, t)
      recalculatePreview()
    }
  }

  private def onToolMoveDown(t: Tool): Unit = {
    val index = tools.getChildren.indexOf(t)
    if (index < tools.getChildren.size() - 1) {
      tools.getChildren.remove(t)
      tools.getChildren.add(index + 1, t)
      recalculatePreview()
    }
  }

  private def onToolChange(t: Tool): Unit = {
    recalculatePreview()
  }

  private def recalculatePreview(): Unit = {
    val gcode = globalModel.originalGCodeData.get
    val previewData = tools.getChildren.asScala.foldLeft(gcode) {
      case (acc, n: DisplaceTool) =>
        gCodeService.transformGCode(
          acc,
          n.model.displaceX.get(),
          n.model.displaceY.get(),
          Math.toRadians(n.model.rotate.get))
    }
    val previewGeometry = gCodeService.gCodeToSegments(previewData)
    model.previewData.set(previewData)
    model.previewGeometry.set(previewGeometry)
  }
}
