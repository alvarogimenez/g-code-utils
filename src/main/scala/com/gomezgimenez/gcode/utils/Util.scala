package com.gomezgimenez.gcode.utils

object Util {
  def windowTitle(fileName: Option[String] = None) =
    s"G-Code Utils - ${fileName.map(_ + " - ").getOrElse("")}Álvaro Gómez Giménez"
}
