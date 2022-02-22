package com.gomezgimenez.gcode.utils

import com.gomezgimenez.gcode.utils.services.GCodeService
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GCodeServiceSpec extends AnyFlatSpec with Matchers {
  behavior of "GCodeService"

  val gCodeService: GCodeService = GCodeService()

  it should "transform a simple G-Code program" in {
    val program =
      Vector(
        "(Sample comment)",
        "G21",
        "G90",
        "M5",
        "G00 Z5.000",
        "M03 S100.0",
        "G00 X0Y0",
        "G01 F25.00",
        "G01 Z-0.1000",
        "G01 F120.0",
        "G01 X10.720 Y-5.13",
        "G01 Y-6"
      )
    val translatedProgram =
      gCodeService
        .transformGCode(
          gCode = gCodeService.parseGCodeLines(program),
          dx = 10,
          dy = 0,
          r = 0
        )
        .map(_.print)

    translatedProgram shouldBe Vector(
      "(Sample comment)",
      "G21",
      "G90",
      "M5",
      "G00 Z5.000",
      "M03 S100.0",
      "G00 X10.000 Y0.000",
      "G01 F25.000",
      "G01 Z-0.100",
      "G01 F120.000",
      "G01 X20.720 Y-5.130",
      "G01 X20.720 Y-6.000"
    )
  }
}
