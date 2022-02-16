package com.gomezgimenez.gcode.utils.model

import javafx.beans.property.{ IntegerProperty, SimpleDoubleProperty, SimpleIntegerProperty, SimpleObjectProperty }

case class LaserTestToolModel() {
  val maxLaserPower: SimpleIntegerProperty                = new SimpleIntegerProperty(1000)
  val feedRate: SimpleIntegerProperty                     = new SimpleIntegerProperty(1000)
  val numberOfProbesObject: SimpleObjectProperty[Integer] = new SimpleObjectProperty[Integer](20)
  val numberOfProbes: IntegerProperty                     = IntegerProperty.integerProperty(numberOfProbesObject)
  val minProbePower: SimpleIntegerProperty                = new SimpleIntegerProperty(5)
  val maxProbePower: SimpleIntegerProperty                = new SimpleIntegerProperty(100)
  val orientation: SimpleObjectProperty[Orientation]      = new SimpleObjectProperty[Orientation](Vertical)

  val boxWidth: SimpleDoubleProperty   = new SimpleDoubleProperty(5.0)
  val boxHeight: SimpleDoubleProperty  = new SimpleDoubleProperty(2.3)
  val spacing: SimpleDoubleProperty    = new SimpleDoubleProperty(0.5)
  val fontWidth: SimpleDoubleProperty  = new SimpleDoubleProperty(1.27)
  val fontHeight: SimpleDoubleProperty = new SimpleDoubleProperty(1.524)

  val segments: SimpleObjectProperty[List[SegmentsWithPower]] = new SimpleObjectProperty[List[SegmentsWithPower]](List.empty)
}
