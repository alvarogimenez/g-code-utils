package com.gomezgimenez.gcode.utils.converters

import com.gomezgimenez.gcode.utils.entities.GBlock
import javafx.util.StringConverter

class GBlockStringConverter extends StringConverter[GBlock] {
  override def toString(p: GBlock): String = p.print

  override def fromString(string: String): GBlock = ???
}
