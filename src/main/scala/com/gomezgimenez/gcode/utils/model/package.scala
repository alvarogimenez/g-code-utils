package com.gomezgimenez.gcode.utils

import com.gomezgimenez.gcode.utils.entities.geometry.Segment

package object model {
  sealed trait Orientation
  case object Horizontal extends Orientation
  case object Vertical   extends Orientation

  case class SegmentsWithPower(
      power: Double,
      boxSegments: List[Segment],
      textSegments: List[Segment]
  )
}
