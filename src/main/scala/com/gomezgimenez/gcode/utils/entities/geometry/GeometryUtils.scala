package com.gomezgimenez.gcode.utils.entities.geometry

import org.apache.commons.math.linear.{MatrixUtils, SingularValueDecompositionImpl}

object GeometryUtils {

  def centroid(points: List[Point]): Point =
    Point(points.map(_.x).sum / points.length, points.map(_.y).sum / points.length)

  /**
   * Calculate rotation using Kabsch algorithm
   * See: https://en.wikipedia.org/wiki/Kabsch_algorithm
   */
  def rotation(aPoints: List[Point], bPoints: List[Point]): Double = {
    val Centroid_A = centroid(aPoints)
    val Centroid_B = centroid(bPoints)
    val A_Points_Center = aPoints.map(p => p.copy(x = p.x - Centroid_A.x, y = p.y - Centroid_A.y)).toArray
    val B_Points_Center = bPoints.map(p => p.copy(x = p.x - Centroid_B.x, y = p.y - Centroid_B.y)).toArray
    val A = MatrixUtils.createRealMatrix(Array(A_Points_Center.map(_.x), A_Points_Center.map(_.y)))
    val B = MatrixUtils.createRealMatrix(Array(B_Points_Center.map(_.x), B_Points_Center.map(_.y)))
    val H = A.multiply(B.transpose())
    val SVD = new SingularValueDecompositionImpl(H)
    val R = SVD.getV.multiply(SVD.getU.transpose())
    Math.toDegrees(Math.atan2(R.getData()(1)(0), R.getData()(0)(0)))
  }
}
