package com.gomezgimenez.gcode.utils

import com.gomezgimenez.gcode.utils.model.GlobalModel
import com.gomezgimenez.gcode.utils.services.GCodeService
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage

import java.io.File
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

package object controller {
  def load(primaryStage: Stage, globalModel: GlobalModel, gCodeService: GCodeService, onSuccess: () => Unit): Future[Unit] = {
    import javafx.stage.FileChooser
    val fileChooser = new FileChooser
    val initialDirectory = if(globalModel.lastDirectory.get.exists()) {
      globalModel.lastDirectory.get
    } else {
      new File(".")
    }
    fileChooser.setInitialDirectory(initialDirectory)
    fileChooser.setTitle("Open G-Code File")
    fileChooser.getExtensionFilters.addAll(new ExtensionFilter("G-Code Files", "*.nc", "*.gcode"), new ExtensionFilter("All Files", "*.*"))
    val selectedFile = fileChooser.showOpenDialog(primaryStage)
    if (selectedFile != null) {
      globalModel.loading.set(true)
      globalModel.loadingText.set(s"""Loading file "${selectedFile.getName}"...""")
      Future {
        gCodeService
          .readGCodeFile(selectedFile)
          .fold(
            error =>
              Platform.runLater(() => {
                val alert = new Alert(AlertType.ERROR)
                alert.setTitle("Error loading file")
                alert.setHeaderText(s"File '${selectedFile.getName}' couldn't be processed.")
                alert.setContentText(
                  s"The parser failed while processing the payload '${error.payload}' due to '${error.message}'.\n" +
                  "Please check if the GCode file was generated correctly and is not empty. " +
                  "If you think this is a bug, please submit an issue to: " +
                  "https://github.com/alvarogimenez/g-code-utils/issues")

                alert.showAndWait()

                globalModel.lastDirectory.set(new File(selectedFile.getParent))
                globalModel.loading.set(false)
              }),
            gCode => {
              val gCodeSegments = gCodeService.gCodeToSegments(gCode)
              Platform.runLater(() => {
                globalModel.lastDirectory.set(new File(selectedFile.getParent))
                globalModel.originalFile.set(selectedFile.getAbsolutePath)
                globalModel.originalGCodeData.set(gCode)
                globalModel.originalGCodeGeometry.set(gCodeSegments)
                globalModel.editedGCodeData.set(gCode)
                globalModel.editedGCodeGeometry.set(gCodeSegments)
                primaryStage.setTitle(Util.windowTitle(Some(selectedFile.getName)))
                onSuccess()
                globalModel.loading.set(false)
              })
            }
          )
      }
    } else {
      Future.successful(())
    }
  }
}
