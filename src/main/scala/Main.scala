import com.gomezgimenez.gcode.utils.{Context, Util}
import javafx.application.{Application, Platform}
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.{Stage, WindowEvent}

class Main extends Application {
  override def start(primaryStage: Stage): Unit = {
    val loader = new FXMLLoader()
    loader.setLocation(Thread.currentThread.getContextClassLoader.getResource("ui/view/MainWindow.fxml"))
    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA)
    val rootLayout = loader.load().asInstanceOf[BorderPane]

    Context.primaryStage = primaryStage
    primaryStage.setOnCloseRequest(new EventHandler[WindowEvent]() {
      override def handle(event: WindowEvent): Unit = {
        Platform.exit()
      }
    })
    val scene = new Scene(rootLayout, 800, 600)
    primaryStage.setScene(scene)
    primaryStage.setTitle(Util.windowTitle())
    primaryStage.setMinWidth(800)
    primaryStage.setMinHeight(600)
    primaryStage.show()
  }
}


