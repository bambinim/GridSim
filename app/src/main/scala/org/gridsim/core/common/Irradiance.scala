package org.gridsim.core.common

import cats.{Order, Show}

/** Represents solar irradiance (W/m²). */
opaque type Irradiance = Double

object Irradiance:
  def apply(v: Double): Irradiance =
    require(v >= 0.0, s"Irradiance cannot be negative: $v")
    v

  val Zero: Irradiance = 0.0

  given Order[Irradiance] = cats.instances.double.catsKernelStdOrderForDouble
  given Show[Irradiance] = Show.show(i => f"${i}%.2f W/m²")

extension (i: Irradiance)
  def toDouble: Double = i

extension (d: Double)
  def wm2: Irradiance = Irradiance(d)
