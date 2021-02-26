import com.gomezgimenez.gcode.utils.Util
import com.gomezgimenez.gcode.utils.controller.MainWindowController
import com.gomezgimenez.gcode.utils.model.DataModel
import com.gomezgimenez.gcode.utils.services.{ConfigService, GCodeService}
import javafx.application.{Application, Platform}
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class Main extends Application {
  private val gCodeService = GCodeService()
  private val configService = ConfigService()
  private val model = DataModel(gCodeService)

  override def start(primaryStage: Stage): Unit = {
    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA)

    val loader = new FXMLLoader()
    loader.setControllerFactory(_ => MainWindowController(primaryStage, gCodeService, configService, model))
    loader.setLocation(getClass.getResource("ui/view/MainWindow.fxml"))

    val rootLayout = loader.load().asInstanceOf[StackPane]
    val scene = new Scene(rootLayout, 800, 600)
    primaryStage.setScene(scene)
    primaryStage.setTitle(Util.windowTitle())
    primaryStage.setMinWidth(scene.getWidth)
    primaryStage.setMinHeight(scene.getHeight)
    primaryStage.setOnCloseRequest(_ => {
      configService.saveConfiguration(configService.buildConfiguration(model))
      Platform.exit()
    })
    scene.getStylesheets.add(getClass.getResource("/ui/style/main.css").toExternalForm)

    primaryStage.getIcons.add(new Image(getClass.getResourceAsStream("icon/favicon.png")));
    primaryStage.show()
  }
}


