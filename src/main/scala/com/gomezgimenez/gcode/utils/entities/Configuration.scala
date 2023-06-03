package com.gomezgimenez.gcode.utils.entities

import com.gomezgimenez.gcode.utils.entities.geometry.Frame

case class Configuration(
    alignmentFrames: Option[AlignmentFrames]
)

case class AlignmentFrames(
    originalFrame: Frame,
    measuredFrame: Frame
)
