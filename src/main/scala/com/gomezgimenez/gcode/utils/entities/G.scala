package com.gomezgimenez.gcode.utils.entities

import java.util.Locale

sealed trait G {
  def print: String
}

case class G_Unknown(raw: String) extends G {
  def print: String = raw
}

case class G_Planar_Motion(
    index: Int,
    x: Double,
    y: Double,
    z: Option[Double],
    f: Option[Double],
    tail: Option[String] = None
) extends G {
  def print: String =
    (
      List(
        s"G${String.format("%02d", index)}",
        s"X${String.format(Locale.US, "%.3f", x)}",
        s"Y${String.format(Locale.US, "%.3f", y)}"
      ) ++
      z.map(z => s"Z${String.format(Locale.US, "%.3f", z)}").toList ++
      f.map(f => s"F${String.format(Locale.US, "%.3f", f)}").toList ++
      tail.toList
    ).mkString(" ")
}

case class G_Motion(
    index: Int,
    x: Option[Double] = None,
    y: Option[Double] = None,
    z: Option[Double] = None,
    f: Option[Double] = None,
    tail: Option[String] = None
) extends G {
  def print: String =
    (
      List(s"G${String.format("%02d", index)}") ++
      x.map(x => s"X${String.format(Locale.US, "%.3f", x)}").toList ++
      y.map(y => s"Y${String.format(Locale.US, "%.3f", y)}").toList ++
      z.map(z => s"Z${String.format(Locale.US, "%.3f", z)}").toList ++
      f.map(f => s"F${String.format(Locale.US, "%.3f", f)}").toList ++
      tail.toList
    ).mkString(" ")
}

object G_Motion {
  val G_Motion_Pattern =
    """(G([0-9]+))\s*(X([0-9.\-]+))?\s*(Y([0-9.\-]+))?\s*(Z([0-9.\-]+))?\s*(F([0-9.\-]+))?\s*(.+)?""".r

  def apply(
      index: Int,
      x: Option[Double] = None,
      y: Option[Double] = None,
      z: Option[Double] = None,
      f: Option[Double] = None,
      tail: Option[String] = None
  ): G_Motion = new G_Motion(index, x, y, z, f, tail)

  def parse(s: String): Option[G] =
    s match {
      case G_Motion_Pattern(_, index, _, x, _, y, _, z, _, f, tail) =>
        Some(
          G_Motion(
            index = index.toInt,
            x = Option(x).map(_.toDouble),
            y = Option(y).map(_.toDouble),
            z = Option(z).map(_.toDouble),
            f = Option(f).map(_.toDouble),
            tail = Option(tail)
          )
        )
      case _ =>
        None
    }
}
