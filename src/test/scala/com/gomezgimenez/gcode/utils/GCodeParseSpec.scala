package com.gomezgimenez.gcode.utils

import com.gomezgimenez.gcode.utils.entities.{ GCommand, G_Motion }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GCodeParseSpec extends AnyFlatSpec with Matchers {
  behavior of "G parser"

  it should "parse common G-Code patterns" in {
    G_Motion.parse("G01 X32") shouldBe Some(G_Motion(1, Some(32.0)))
    G_Motion.parse("G01 X32 Y-13.4") shouldBe Some(G_Motion(1, Some(32.0), Some(-13.4)))
    G_Motion.parse("G01 X32 Y-13.4 Z0.5") shouldBe Some(G_Motion(1, Some(32.0), Some(-13.4), Some(0.5)))
    G_Motion.parse("G01 X32 Y-13.4 Z0.5 F150") shouldBe Some(G_Motion(1, Some(32.0), Some(-13.4), Some(0.5), Some(150.0)))

    G_Motion.parse("G01 X32") shouldBe Some(G_Motion(1, Some(32.0)))
    G_Motion.parse("G01 X32Y-13.4") shouldBe Some(G_Motion(1, Some(32.0), Some(-13.4)))
    G_Motion.parse("G01 X32Y-13.4Z0.5") shouldBe Some(G_Motion(1, Some(32.0), Some(-13.4), Some(0.5)))
    G_Motion.parse("G01 X32Y-13.4Z0.5F150") shouldBe Some(G_Motion(1, Some(32.0), Some(-13.4), Some(0.5), Some(150.0)))

    G_Motion.parse("G01 X32 F150") shouldBe Some(G_Motion(1, x = Some(32.0), f = Some(150.0)))
  }

  it should "parse G00, G0, G01 and G1 commands" in {
    G_Motion.parse("G00 X32 Y-13.4") shouldBe Some(G_Motion(0, Some(32.0), Some(-13.4)))
    G_Motion.parse("G0 X32 Y-13.4") shouldBe Some(G_Motion(0, Some(32.0), Some(-13.4)))
    G_Motion.parse("G01 X32 Y-13.4") shouldBe Some(G_Motion(1, Some(32.0), Some(-13.4)))
    G_Motion.parse("G1 X32 Y-13.4") shouldBe Some(G_Motion(1, Some(32.0), Some(-13.4)))
  }

  it should "parse unmanaged tail in G00 and G01 commands" in {
    G_Motion.parse("G0 X32 Y-13.4 S1000 M3") shouldBe Some(G_Motion(0, Some(32.0), Some(-13.4), tail = Some("S1000 M3")))
  }

  it should "format G objects into string commands" in {
    G_Motion(1, Some(32.0), Some(-13.4), Some(0.5), Some(150.0)).print shouldBe "G01 X32.000 Y-13.400 Z0.500 F150.000"
    G_Motion(1, Some(32.0), Some(-13.4), Some(0.5)).print shouldBe "G01 X32.000 Y-13.400 Z0.500"
    G_Motion(1, Some(32.0), Some(-13.4)).print shouldBe "G01 X32.000 Y-13.400"
    G_Motion(1, Some(32.0)).print shouldBe "G01 X32.000"

    G_Motion(1, x = Some(32.0), f = Some(150.0)).print shouldBe "G01 X32.000 F150.000"
  }
}
