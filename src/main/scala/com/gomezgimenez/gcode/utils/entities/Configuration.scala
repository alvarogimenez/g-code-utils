package com.gomezgimenez.gcode.utils.entities

case class Configuration(
                          alignmentFrames: Option[AlignmentFrames]
                        )

case class AlignmentFrames(
                            originalFrame: Frame,
                            measuredFrame: Frame
                          )
