package org.gridsim.core.common

import cats.{Order, Show}
import cats.kernel.CommutativeMonoid
import java.util.concurrent.TimeUnit
import scala.annotation.targetName
import scala.concurrent.duration.FiniteDuration

import Formatting.*

/** Represents electrical energy (kWh). */
opaque type Energy = Double

object Energy:
  def apply(v: Double): Energy = v
  val Zero: Energy = 0.0

  given CommutativeMonoid[Energy] =
    cats.instances.double.catsKernelStdGroupForDouble
  given Order[Energy] = cats.instances.double.catsKernelStdOrderForDouble
  given showEnergy: Show[Energy] = Show.show(e => s"${e.show2} kWh")

  extension (e: Energy)
    def toDouble: Double = e
    @targetName("energyPlus") def +(o: Energy): Energy = e + o
    @targetName("energyMinus") def -(o: Energy): Energy = e - o
    @targetName("energyUnaryMinus") def unary_- : Energy = -e
    @targetName("energyTimes") def *(scalar: Double): Energy = e * scalar
    @targetName("energyDiv") def /(o: Energy): Double = e / o
    @targetName("energyDivScalar") def /(scalar: Double): Power = Power(
      e / scalar
    )
    def toPower(using tick: FiniteDuration): Power = Power(
      e / tick.toUnit(TimeUnit.HOURS)
    )
    def min(o: Energy): Energy = if e <= o then e else o
    def max(o: Energy): Energy = if e >= o then e else o
    def abs: Energy = Math.abs(e.toDouble).kwh
    def toFlow: Flow[Energy] =
      if e > 0.0 then Flow.Surplus(e)
      else if e < 0.0 then Flow.Deficit(e.abs)
      else Flow.Balanced

extension (d: Double) def kwh: Energy = Energy(d)

extension (i: Integer) def kwh: Energy = Energy(i.toDouble)
