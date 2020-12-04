package com.gomezgimenez.gcode.utils

import org.json4s.Formats
import org.json4s.native.Serialization

package object services {
  def fromJson[T <: AnyRef : Manifest](source: String)(implicit f: Formats): T = {
    Serialization.read[T](source)
  }

  def toJson[T <: AnyRef: Manifest](t: T)(implicit f: Formats): String = {
    Serialization.writePretty(t)
  }
}
