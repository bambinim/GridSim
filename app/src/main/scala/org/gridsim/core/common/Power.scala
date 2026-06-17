package org.gridsim.core.common

import cats.{Order, Show}
import cats.kernel.CommutativeMonoid
import java.util.Locale
import java.lang.Math.abs
import java.util.concurrent.TimeUnit
import scala.annotation.targetName
import scala.concurrent.duration.FiniteDuration

opaque type Power = Double

object Power:
  def apply(v: Double): Power = v
  val Zero: Power = 0.0

  given CommutativeMonoid[Power] = cats.instances.double.catsKernelStdGroupForDouble
  given Order[Power] = cats.instances.double.catsKernelStdOrderForDouble
  given showPower: Show[Power] = Show.show(p => String.format(java.util.Locale.US, "%.2f kW", p.toDouble))

  extension (p: Power)
    def toDouble: Double = p
    @targetName("powerPlus") def +(o: Power): Power = p + o
    @targetName("powerMinus") def -(o: Power): Power = p - o
    @targetName("powerUnaryMinus") def unary_- : Power = -p
    @targetName("powerTimes") def *(scalar: Double): Power = p * scalar
    @targetName("powerDiv") def /(o: Power): Double = p / o
    @targetName("powerDivScalar") def /(scalar: Double): Power = p / scalar
    def toEnergy(using tick: FiniteDuration): Energy = Energy(p * tick.toUnit(TimeUnit.HOURS))
    def min(o: Power): Power = if p <= o then p else o
    def max(o: Power): Power = if p >= o then p else o
    def abs: Power = Math.abs(p.toDouble).kw

extension (d: Double)
  def kw: Power = Power(d)
