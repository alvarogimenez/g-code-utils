package com.gomezgimenez.gcode.utils.controller

import com.gomezgimenez.gcode.utils.components.EditorPlot
import com.gomezgimenez.gcode.utils.components.editor.{DisplaceTool, PanelTool, RotateTool, Tool}
import com.gomezgimenez.gcode.utils.entities.GBlock
import com.gomezgimenez.gcode.utils.entities.geometry.Geometry
import com.gomezgimenez.gcode.utils.model.editor.{DisplaceModel, PanelModel, RotateModel}
import com.gomezgimenez.gcode.utils.model.{EditorModel, GlobalModel}
import com.gomezgimenez.gcode.utils.services.GCodeService
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Button, MenuItem}
import javafx.scene.layout.{BorderPane, VBox}
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage

import scala.concurrent.ExecutionContext.Implicits.global
import java.io.{BufferedWriter, File, FileWriter}
import scala.concurrent.Future
import scala.jdk.CollectionConverters.CollectionHasAsScala

case class EditorTabController(
    primaryStage: Stage,
    gCodeService: GCodeService,
    globalModel: GlobalModel,
    model: EditorModel
) {

  @FXML private var tools: VBox                 = _
  @FXML private var editor_canvas: BorderPane   = _
  @FXML private var button_open: Button         = _
  @FXML private var button_save_as: Button      = _
  @FXML private var add_tool_displace: MenuItem = _
  @FXML private var add_tool_rotate: MenuItem   = _
  @FXML private var add_tool_panel: MenuItem    = _

  def initialize(): Unit = {
    editor_canvas.setCenter(EditorPlot(model, globalModel))

    add_tool_displace.setOnAction((_: ActionEvent) => {
      tools.getChildren.add(
        DisplaceTool(
          model = DisplaceModel(),
          onDelete = onToolDelete,
          onMoveUp = onToolMoveUp,
          onMoveDown = onToolMoveDown,
          onChange = onToolChange
        ))
    })

    add_tool_rotate.setOnAction((_: ActionEvent) => {
      tools.getChildren.add(
        RotateTool(
          model = RotateModel(),
          onDelete = onToolDelete,
          onMoveUp = onToolMoveUp,
          onMoveDown = onToolMoveDown,
          onChange = onToolChange
        ))
    })

    add_tool_panel.setOnAction((_: ActionEvent) => {
      tools.getChildren.add(
        PanelTool(
          model = PanelModel(),
          onDelete = onToolDelete,
          onMoveUp = onToolMoveUp,
          onMoveDown = onToolMoveDown,
          onChange = onToolChange
        ))
    })

    button_open.setOnAction((_: ActionEvent) => {
      load(primaryStage, globalModel, gCodeService, () => {
        recalculatePreview()
      })
    })

    button_save_as.disableProperty().bind(globalModel.editedGCodeData.isNotEqualTo(Vector.empty).not())
    button_save_as.setOnAction((_: ActionEvent) => {
      import javafx.stage.FileChooser
      if (globalModel.originalFile.get != null) {
        val file                     = new File(globalModel.originalFile.get)
        val fileNameWithoutExtension = file.getName.split("\\.").dropRight(1).mkString(".")
        val fileNameExtension        = file.getName.split("\\.").last
        val fileChooser              = new FileChooser
        fileChooser.setInitialDirectory(file.getParentFile)
        fileChooser.setInitialFileName(fileNameWithoutExtension + "_edited" + "." + fileNameExtension)
        fileChooser.setTitle("Save Edited G-Code File")
        fileChooser.getExtensionFilters.addAll(new ExtensionFilter("G-Code Files", "*.nc", "*.gcode"), new ExtensionFilter("All Files", "*.*"))
        val selectedFile = fileChooser.showSaveDialog(primaryStage)
        if (selectedFile != null) {
          val gCode = globalModel.editedGCodeData.get
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
    onToolChange(t)
  }

  private def onToolMoveUp(t: Tool): Unit = {
    val index = tools.getChildren.indexOf(t)
    if (index > 0) {
      tools.getChildren.remove(t)
      tools.getChildren.add(index - 1, t)
      onToolChange(t)
    }
  }

  private def onToolMoveDown(t: Tool): Unit = {
    val index = tools.getChildren.indexOf(t)
    if (index < tools.getChildren.size() - 1) {
      tools.getChildren.remove(t)
      tools.getChildren.add(index + 1, t)
      onToolChange(t)
    }
  }

  private def onToolChange(t: Tool): Unit = {
    globalModel.loading.set(true)
    globalModel.loadingText.set(s"""Calculating tools...""")
    recalculatePreview().foreach { case (data, geometry) =>
      Platform.runLater(() => {
        globalModel.editedGCodeData.set(data)
        globalModel.editedGCodeGeometry.set(geometry)
        globalModel.loading.set(false)
      })
    }
  }

  private def recalculatePreview(): Future[(Vector[GBlock], Vector[Geometry])] = {
    Future {
      val gcode = globalModel.originalGCodeData.get
      val previewData = tools.getChildren.asScala.foldLeft(gcode) {
        case (acc, n: DisplaceTool) =>
          gCodeService.rotateAndDisplace(gCode = acc, dx = n.model.displaceX.get(), dy = n.model.displaceY.get())
        case (acc, n: RotateTool) =>
          gCodeService.rotateAndDisplace(gCode = acc, cx = n.model.centerX.get(), cy = n.model.centerY.get(), r = Math.toRadians(n.model.angle.get()))
        case (acc, n: PanelTool) =>
          val xRepeat = n.model.panelXObject.get()
          val yRepeat = n.model.panelYObject.get()
          val xSpacing = n.model.spacingX.get()
          val ySpacing = n.model.spacingY.get()

          val (negXClones, posXClones) = {
            if (n.model.xAlignLeft.get()) (0, xRepeat - 1)
            else if (n.model.xAlignRight.get()) (xRepeat - 1, 0)
            else ((xRepeat - 1) / 2, (xRepeat - 1) / 2 + (xRepeat - 1) % 2)
          }
          val (negYClones, posYClones) = {
            if (n.model.yAlignTop.get()) (yRepeat - 1, 0)
            else if (n.model.yAlignBottom.get()) (0, yRepeat - 1)
            else ((yRepeat - 1) / 2, (yRepeat - 1) / 2 + (yRepeat - 1) % 2)
          }
          println(s"xRepeat = $xRepeat, yRepeat = $yRepeat, negXClones = $negXClones, posXClones = $posXClones")

          (-negXClones to posXClones).flatMap { x =>
            (-negYClones to posYClones).flatMap { y =>
              gCodeService.rotateAndDisplace(
                gCode = acc,
                dx = x * xSpacing,
                dy = y * ySpacing
              )
            }
          }.toVector
      }
      val previewGeometry = gCodeService.gCodeToSegments(previewData)
      (previewData, previewGeometry)
    }
  }
}
