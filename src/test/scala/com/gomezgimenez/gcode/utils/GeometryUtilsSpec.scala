package com.gomezgimenez.gcode.utils

import com.gomezgimenez.gcode.utils.entities.geometry._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GeometryUtilsSpec extends AnyFlatSpec with Matchers {
  behavior of "GeometryUtils"

  val rotatedFrame60x30: Frame = Frame(
    Point(-29.193, 2.408),
    Point(27.573, 21.841),
    Point(37.29, -6.542),
    Point(-19.476, -25.975)
  )

  it should "calculate rotation of a 0-centered frame" in {
    val a = Frame(Point(-30,15), Point(30,15), Point(30,-15), Point(-30,-15))
    a.angle(rotatedFrame60x30) shouldBe 18.90 +- 0.01
  }

  it should "calculate rotation of a non 0-centered frame" in {
    val a = Frame(Point(0,30), Point(60,30), Point(60,0), Point(0,0))
    a.angle(rotatedFrame60x30) shouldBe 18.90 +- 0.01
  }
}
