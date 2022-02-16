import com.gomezgimenez.gcode.utils.Util
import com.gomezgimenez.gcode.utils.controller.{ AlignToolTabController, LaserTestTabController, MainWindowController }
import com.gomezgimenez.gcode.utils.model.{ AlignToolModel, GlobalModel, LaserTestToolModel }
import com.gomezgimenez.gcode.utils.services.{ ConfigService, GCodeService }
import javafx.application.{ Application, Platform }
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class Main extends Application {
  private val gCodeService       = GCodeService()
  private val configService      = ConfigService()
  private val globalModel        = GlobalModel()
  private val alignToolModel     = AlignToolModel(gCodeService)
  private val laserTestToolModel = LaserTestToolModel()

  override def start(primaryStage: Stage): Unit = {
    configService.loadConfiguration.foreach { config =>
      configService.populateModelFromConfig(
        config,
        globalModel,
        alignToolModel
      )
    }

    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA)

    val loader = new FXMLLoader()
    loader.setControllerFactory({
      case c if c == classOf[MainWindowController] =>
        MainWindowController(primaryStage, globalModel)
      case c if c == classOf[AlignToolTabController] =>
        AlignToolTabController(primaryStage, gCodeService, globalModel, alignToolModel)
      case c if c == classOf[LaserTestTabController] =>
        LaserTestTabController(primaryStage, gCodeService, globalModel, laserTestToolModel)
    })
    loader.setLocation(getClass.getResource("ui/view/MainWindow.fxml"))

    val rootLayout = loader.load().asInstanceOf[StackPane]
    val scene      = new Scene(rootLayout, 800, 600)
    primaryStage.setScene(scene)
    primaryStage.setTitle(Util.windowTitle())
    primaryStage.setMinWidth(scene.getWidth)
    primaryStage.setMinHeight(scene.getHeight)
    primaryStage.setOnCloseRequest(_ => {
      configService.saveConfiguration(configService.buildConfiguration(globalModel, alignToolModel))
      Platform.exit()
    })
    scene.getStylesheets.add(getClass.getResource("/ui/style/main.css").toExternalForm)

    primaryStage.getIcons.add(new Image(getClass.getResourceAsStream("icon/favicon.png")));
    primaryStage.show()
  }
}
