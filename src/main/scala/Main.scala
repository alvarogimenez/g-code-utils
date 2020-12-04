import com.gomezgimenez.gcode.utils.Util
import com.gomezgimenez.gcode.utils.controller.MainWindowController
import com.gomezgimenez.gcode.utils.entities.{AlignmentFrames, Configuration}
import com.gomezgimenez.gcode.utils.model.DataModel
import com.gomezgimenez.gcode.utils.services.{ConfigService, GCodeService}
import javafx.application.{Application, Platform}
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class Main extends Application {
  private val gCodeService = GCodeService()
  private val configService = ConfigService()
  private val model = DataModel(gCodeService)

  override def start(primaryStage: Stage): Unit = {
    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA)

    val loader = new FXMLLoader()
    loader.setControllerFactory(_ => MainWindowController(primaryStage, gCodeService, configService, model))
    loader.setLocation(Thread.currentThread.getContextClassLoader.getResource("ui/view/MainWindow.fxml"))

    val rootLayout = loader.load().asInstanceOf[BorderPane]
    val scene = new Scene(rootLayout, 800, 600)
    primaryStage.setScene(scene)
    primaryStage.setTitle(Util.windowTitle())
    primaryStage.setMinWidth(scene.getWidth)
    primaryStage.setMinHeight(scene.getHeight)
    primaryStage.setOnCloseRequest(_ => {
      configService.saveConfiguration(configService.buildConfiguration(model))
      Platform.exit()
    })
    primaryStage.show()
  }
}


