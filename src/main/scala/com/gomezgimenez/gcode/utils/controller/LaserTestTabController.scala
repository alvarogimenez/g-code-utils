package com.gomezgimenez.gcode.utils.controller

import java.io.{ BufferedWriter, File, FileWriter }
import java.lang
import java.text.DecimalFormat
import java.util.Locale

import com.gomezgimenez.gcode.utils.components.LaserTestPlot
import com.gomezgimenez.gcode.utils.entities.{ Point, Segment }
import com.gomezgimenez.gcode.utils.model._
import com.gomezgimenez.gcode.utils.services.GCodeService
import javafx.beans.{ InvalidationListener, Observable }
import javafx.beans.binding.Bindings
import javafx.beans.value.{ ChangeListener, ObservableValue }
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage
import javafx.util.converter.NumberStringConverter

case class LaserTestTabController(
    primaryStage: Stage,
    gCodeService: GCodeService,
    globalModel: GlobalModel,
    model: LaserTestToolModel
) {
  @FXML var laser_test_tool_canvas: BorderPane             = _
  @FXML var text_field_max_laser_power: TextField          = _
  @FXML var text_field_feed_rate: TextField                = _
  @FXML var text_field_min_probe_power: TextField          = _
  @FXML var text_field_max_probe_power: TextField          = _
  @FXML var text_field_box_width: TextField                = _
  @FXML var text_field_box_height: TextField               = _
  @FXML var text_field_spacing: TextField                  = _
  @FXML var text_field_text_height: TextField              = _
  @FXML var text_field_text_width: TextField               = _
  @FXML var spinner_number_of_probes: Spinner[Integer]     = _
  @FXML var choice_box_orientation: ChoiceBox[Orientation] = _
  @FXML var button_save_as: Button                         = _

  def initialize(): Unit = {
    laser_test_tool_canvas.setCenter(LaserTestPlot(model))

    choice_box_orientation.getItems.add(Horizontal)
    choice_box_orientation.getItems.add(Vertical)
    choice_box_orientation.valueProperty().bindBidirectional(model.orientation)

    spinner_number_of_probes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1))
    spinner_number_of_probes.getValueFactory.valueProperty().bindBidirectional(model.numberOfProbesObject)
    spinner_number_of_probes
      .focusedProperty()
      .addListener(new ChangeListener[lang.Boolean] {
        override def changed(observable: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit =
          if (!newValue) {
            spinner_number_of_probes.increment(0)
          }
      })

    val c = new NumberStringConverter(Locale.ENGLISH, "#.#")

    Bindings.bindBidirectional(text_field_max_laser_power.textProperty(), model.maxLaserPower, c)
    Bindings.bindBidirectional(text_field_feed_rate.textProperty(), model.feedRate, c)
    Bindings.bindBidirectional(text_field_min_probe_power.textProperty(), model.minProbePower, c)
    Bindings.bindBidirectional(text_field_max_probe_power.textProperty(), model.maxProbePower, c)
    Bindings.bindBidirectional(text_field_box_width.textProperty(), model.boxWidth, c)
    Bindings.bindBidirectional(text_field_box_height.textProperty(), model.boxHeight, c)
    Bindings.bindBidirectional(text_field_spacing.textProperty(), model.spacing, c)
    Bindings.bindBidirectional(text_field_text_height.textProperty(), model.fontHeight, c)
    Bindings.bindBidirectional(text_field_text_width.textProperty(), model.fontWidth, c)

    button_save_as.setOnAction((_: ActionEvent) => save())

    model.numberOfProbes.addListener(_ => calculateLaserTest())
    model.maxLaserPower.addListener(_ => calculateLaserTest())
    model.minProbePower.addListener(_ => calculateLaserTest())
    model.maxProbePower.addListener(_ => calculateLaserTest())
    model.orientation.addListener(_ => calculateLaserTest())
    model.boxWidth.addListener(_ => calculateLaserTest())
    model.boxHeight.addListener(_ => calculateLaserTest())
    model.spacing.addListener(_ => calculateLaserTest())
    model.fontWidth.addListener(_ => calculateLaserTest())
    model.fontHeight.addListener(_ => calculateLaserTest())

    calculateLaserTest()
  }

  def save(): Unit = {
    import javafx.stage.FileChooser

    val fileChooser = new FileChooser
    fileChooser.setInitialDirectory(new File("."))
    fileChooser.setInitialFileName("laser_test_probe.nc")
    fileChooser.setTitle("Save Laser Test G-Code File")
    fileChooser.getExtensionFilters.addAll(
      new ExtensionFilter("G-Code Files", "*.nc", "*.gcode"),
      new ExtensionFilter("All Files", "*.*")
    )
    val selectedFile = fileChooser.showSaveDialog(primaryStage)
    if (selectedFile != null) {
      val gCode = gCodeService.segmentsToLaserGCode(model.segments.get, model.feedRate.get)
      val bw    = new BufferedWriter(new FileWriter(selectedFile))
      gCode.foreach { line =>
        bw.write(line + "\n")
      }
      bw.close()
    }
  }

  def calculateLaserTest(): Unit = {
    val boxWidth   = model.boxWidth.get
    val boxHeight  = model.boxHeight.get
    val spacing    = model.spacing.get
    val fontWidth  = model.fontWidth.get
    val fontHeight = model.fontHeight.get
    val min        = model.minProbePower.get
    val max        = model.maxProbePower.get
    val total      = model.numberOfProbes.get

    val increment = if (total > 1) {
      (max - min).toDouble / (total - 1)
    } else {
      0
    }

    val segments = (0 until total).map { n =>
      val p = if (model.orientation.get == Vertical) {
        Point(0, n * boxHeight + spacing * n)
      } else {
        Point(n * boxWidth + spacing * n, 0)
      }
      val a     = Point(p.x - boxWidth / 2, p.y + boxHeight / 2)
      val b     = Point(p.x + boxWidth / 2, p.y + boxHeight / 2)
      val c     = Point(p.x + boxWidth / 2, p.y - boxHeight / 2)
      val d     = Point(p.x - boxWidth / 2, p.y - boxHeight / 2)
      val power = min + increment * n

      SegmentsWithPower(
        power = (power / 100.0) * model.maxLaserPower.get,
        segments = List(
          Segment(a, b),
          Segment(b, c),
          Segment(c, d),
          Segment(d, a)
        ) ++ textToSegments(s"${power.toInt}%", Point(p.x, p.y), fontWidth, fontHeight)
      )
    }

    model.segments.set(segments.toList)
  }

  def textToSegments(t: String, p: Point, w: Double, h: Double): List[Segment] =
    t.zipWithIndex.flatMap {
      case (c, index) =>
        symbolToSegments(c, p.copy(x = p.x + w * index - (w * (t.length - 1)) / 2), w, h)
    }.toList

  def symbolToSegments(s: Char, p: Point, w: Double, h: Double): List[Segment] = {
    val outlines = Map(
      '0' -> List(
        Segment(Point(-1, 5), Point(1, 5)),
        Segment(Point(1, 5), Point(3, 3)),
        Segment(Point(3, 3), Point(3, -3)),
        Segment(Point(3, -3), Point(1, -5)),
        Segment(Point(1, -5), Point(-1, -5)),
        Segment(Point(-1, -5), Point(-3, -3)),
        Segment(Point(-3, -3), Point(-3, 3)),
        Segment(Point(-3, 3), Point(-1, 5))
      ),
      '1' -> List(
        Segment(Point(-3, 2), Point(0, 5)),
        Segment(Point(0, 5), Point(0, -5)),
        Segment(Point(-2, -5), Point(2, -5)),
      ),
      '2' -> List(
        Segment(Point(-3, 3), Point(-1, 5)),
        Segment(Point(-1, 5), Point(1, 5)),
        Segment(Point(1, 5), Point(3, 3)),
        Segment(Point(3, 3), Point(3, 1)),
        Segment(Point(3, 1), Point(-3, -5)),
        Segment(Point(-3, -5), Point(3, -5)),
      ),
      '3' -> List(
        Segment(Point(-3, 5), Point(3, 5)),
        Segment(Point(3, 5), Point(0, 1)),
        Segment(Point(0, 1), Point(3, -2)),
        Segment(Point(3, -2), Point(3, -3)),
        Segment(Point(3, -3), Point(1, -5)),
        Segment(Point(1, -5), Point(-2, -5)),
        Segment(Point(-2, -5), Point(-3, -4)),
      ),
      '4' -> List(
        Segment(Point(3, -1), Point(-3, -1)),
        Segment(Point(-3, -1), Point(2, 5)),
        Segment(Point(2, 5), Point(2, -5))
      ),
      '5' -> List(
        Segment(Point(3, 5), Point(-3, 5)),
        Segment(Point(-3, 5), Point(-3, 1)),
        Segment(Point(-3, 1), Point(1, 1)),
        Segment(Point(1, 1), Point(3, -1)),
        Segment(Point(3, -1), Point(3, -3)),
        Segment(Point(3, -3), Point(1, -5)),
        Segment(Point(1, -5), Point(-2, -5)),
        Segment(Point(-2, -5), Point(-3, -4)),
      ),
      '6' -> List(
        Segment(Point(0, 5), Point(-3, -2)),
        Segment(Point(-3, -2), Point(-3, -3)),
        Segment(Point(-3, -3), Point(-1, -5)),
        Segment(Point(-1, -5), Point(1, -5)),
        Segment(Point(1, -5), Point(3, -3)),
        Segment(Point(3, -3), Point(3, -2)),
        Segment(Point(3, -2), Point(1, 0)),
        Segment(Point(1, 0), Point(-2, 0))
      ),
      '7' -> List(
        Segment(Point(-3, 5), Point(3, 5)),
        Segment(Point(3, 5), Point(-3, -5))
      ),
      '8' -> List(
        Segment(Point(-1, 5), Point(1, 5)),
        Segment(Point(1, 5), Point(3.5, 4)),
        Segment(Point(3.5, 4), Point(3.5, 1.5)),
        Segment(Point(3.5, 1.5), Point(1, 0)),
        Segment(Point(1, 0), Point(-1, 0)),
        Segment(Point(-1, 0), Point(-3.5, 1.5)),
        Segment(Point(-3.5, 1.5), Point(-3.5, 4)),
        Segment(Point(-3.5, 4), Point(-1, 5)),
        Segment(Point(1, 0), Point(3.5, -1.5)),
        Segment(Point(3.5, -1.5), Point(3.5, -4)),
        Segment(Point(3.5, -4), Point(1, -5)),
        Segment(Point(1, -5), Point(-1, -5)),
        Segment(Point(-1, -5), Point(-3.5, -4)),
        Segment(Point(-3.5, -4), Point(-3.5, -1.5)),
        Segment(Point(-3.5, -1.5), Point(-1, 0)),
      ),
      '9' -> List(
        Segment(Point(1.5, 0), Point(-1, 0)),
        Segment(Point(-1, 0), Point(-3, 1)),
        Segment(Point(-3, 1), Point(-3, 4)),
        Segment(Point(-3, 4), Point(-1, 5)),
        Segment(Point(-1, 5), Point(1, 5)),
        Segment(Point(1, 5), Point(3, 4)),
        Segment(Point(3, 4), Point(3, 2)),
        Segment(Point(3, 2), Point(-2, -5))
      ),
      '%' -> List(
        Segment(Point(3, 5), Point(-3, -5)),
        Segment(Point(-3, 3), Point(-3, 2)),
        Segment(Point(-3, 2), Point(-2, 2)),
        Segment(Point(-2, 2), Point(-2, 3)),
        Segment(Point(-2, 3), Point(-3, 3)),
        Segment(Point(2, -2), Point(2, -3)),
        Segment(Point(2, -3), Point(3, -3)),
        Segment(Point(3, -3), Point(3, -2)),
        Segment(Point(3, -2), Point(2, -2)),
      )
    )

    outlines(s).map(s => s * 0.1 * Point(w, h) + p)
  }
}
