package com.gomezgimenez.gcode.utils.entities.geometry

import javafx.scene.canvas.GraphicsContext

trait Geometry {
  def boundingBox: BoundingBox
  def plot(g2d: GraphicsContext): Unit
}
