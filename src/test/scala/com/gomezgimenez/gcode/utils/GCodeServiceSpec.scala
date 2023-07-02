package com.gomezgimenez.gcode.utils

import com.gomezgimenez.gcode.utils.entities.GParser
import com.gomezgimenez.gcode.utils.entities.geometry.{Arc, Point, Segment}
import com.gomezgimenez.gcode.utils.services.GCodeService
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GCodeServiceSpec extends AnyFlatSpec with Matchers {
  behavior of "GCodeService"

  val gCodeService: GCodeService = GCodeService()

  it should "transform a simple G-Code program" in {
    val program = {
      Vector(
        "(Sample comment)",
        "G21",
        "G90",
        "M5",
        "G00 Z5.000",
        "M03 S100.0",
        "G00 X0 Y0",
        "G01 F25.00",
        "G01 Z-0.1000",
        "G01 F120.0",
        "G01 X10.720 Y-5.13",
        "G01 Y-6"
      )
    }
    val translatedProgram =
      gCodeService
        .rotateAndDisplace(
          gCode = GParser.parse(program).getOrElse(Vector.empty),
          cx = 0,
          cy = 0,
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
      "G01 F25.00",
      "G01 Z-0.100",
      "G01 F120.0",
      "G01 X20.720 Y-5.130",
      "G01 X20.720 Y-6.000"
    )
  }

  it should "transform G2/G3 arcs to geometry" in {
    val geometry = gCodeService.gCodeToSegments(
      GParser.parse(Vector(
        "G0 X10 Y0",
        "G3 X0 Y10 I-10 J0",
        "G3 X-10 Y0 I0 J-10",
        "G3 X0 Y-10 I10 J0",
        "G3 X10 Y0 I0 J10",
        "G00 X8 Y0",
        "G2 X0 Y-8 I-8 J0",
        "G2 X-8 Y0 I0 J8",
        "G2 X0 Y8 I8 J0",
        "G2 X8 Y0 I0 J-8",
        "G0 X0 Y6",
        "G3 X0 Y-6 I0 J-6",
        "G3 X0 Y6 I0 J6",
        "G0 X0 Y4",
        "G2 X0 Y-4 I0 J-4",
        "G2 X0 Y4 I0 J4",
      )).getOrElse(Vector.empty))

    geometry shouldBe Vector(
      Segment(Point(0, 0), Point(10, 0)),
      Arc(Point(0, 0), 10, -90, 90, 3),
      Arc(Point(0, 0), 10, 180, 90, 3),
      Arc(Point(0, 0), 10, 90, 90, 3),
      Arc(Point(0, 0), 10, 0, 90, 3),
      Segment(Point(10, 0), Point(8, 0)),
      Arc(Point(0, 0), 8, 0, 90, 2),
      Arc(Point(0, 0), 8, 90, 90, 2),
      Arc(Point(0, 0), 8, 180, 90, 2),
      Arc(Point(0, 0), 8, 270, 90, 2),
      Segment(Point(8, 0), Point(0, 6)),
      Arc(Point(0, 0), 6, 90, 180, 3),
      Arc(Point(0, 0), 6, -90, 180, 3),
      Segment(Point(0, 6), Point(0, 4)),
      Arc(Point(0, 0), 4, 270, 180, 2),
      Arc(Point(0, 0), 4, 90, 180, 2),
    )
  }
}
