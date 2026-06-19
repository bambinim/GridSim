package org.gridsim.core.common

import cats.kernel.CommutativeMonoid
import cats.{Order, Show}

/**
 * Represents incident solar irradiance (W/m²).
 *
 * This is a per-unit-area quantity, physically distinct from electrical
 * power ([[Power]] in kW). The conversion to electrical output depends on
 * the panel area and efficiency, and is delegated to producer components
 * (e.g. PV panel in [[House]], [[StandaloneProducer]]).
 *
 * A value of 0.0 indicates no light (night or overcast sky).
 * Typical range: 0–1200 W/m².
 */
opaque type Irradiance = Double

object Irradiance:
  def apply(v: Double): Irradiance =
    require(v >= 0.0, s"Irradiance cannot be negative: $v")
    v

  val Zero: Irradiance = 0.0

  given Order[Irradiance] = cats.instances.double.catsKernelStdOrderForDouble
  given Show[Irradiance] = Show.show(i => s"$i W/m²")

extension (i: Irradiance)
  def toDouble: Double = i

extension (d: Double)
  def wm2: Irradiance = Irradiance(d)
