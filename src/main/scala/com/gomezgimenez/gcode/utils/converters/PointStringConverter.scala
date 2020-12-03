package com.gomezgimenez.gcode.utils.converters

import com.gomezgimenez.gcode.utils.Point
import javafx.util.StringConverter

class PointStringConverter extends StringConverter[Option[Point]] {
  override def toString(p: Option[Point]): String = {
    p.map(_.toString).getOrElse("(--,--)")
  }

  override def fromString(string: String): Option[Point] = Point.fromString(string)
}
