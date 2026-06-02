package org.gridsim.core.common

import cats.{Order, Show}
import cats.kernel.CommutativeMonoid
import cats.instances.double.*
import com.google.common.primitives.UnsignedLong

import java.util.Locale
import java.util.concurrent.TimeUnit
import scala.annotation.targetName
import scala.concurrent.duration.FiniteDuration

object Units:

  opaque type Power = Double

  object Power:
    def apply(v: Double): Power = v
    val Zero: Power = 0.0

    given CommutativeMonoid[Power] = cats.instances.double.catsKernelStdGroupForDouble
    given Order[Power] = cats.instances.double.catsKernelStdOrderForDouble
    given Show[Power] = Show.show(p => String.format(Locale.US, "%.2f kW", p))

  opaque type Energy = Double

  object Energy:
    def apply(v: Double): Energy = v
    val Zero: Energy = 0.0

    given CommutativeMonoid[Energy] = cats.instances.double.catsKernelStdGroupForDouble
    given Order[Energy] = cats.instances.double.catsKernelStdOrderForDouble
    given Show[Energy] = Show.show(e => String.format(Locale.US, "%.2f kWh", e))


  extension (p: Power)
    @targetName("powerToDouble")
    def toDouble: Double = p
    @targetName("powerPlus")
    def +(o: Power): Power = p + o
    @targetName("powerMinus")
    def -(o: Power): Power = p - o
    @targetName("powerTimes")
    def *(scalar: Double): Power = p * scalar
    def toEnergy(using tick: FiniteDuration): Energy =
      p * tick.toUnit(TimeUnit.HOURS)

  extension (e: Energy)
    @targetName("energyToDouble")
    def toDouble: Double = e
    @targetName("energyPlus")
    def +(o: Energy): Energy = e + o
    @targetName("energyMinus")
    def -(o: Energy): Energy = e - o
    @targetName("energyTimes")
    def *(scalar: Double): Energy = e * scalar
    def toPower(using tick: FiniteDuration): Power =
      e / tick.toUnit(TimeUnit.HOURS)

  extension (d: Double)
    def kw: Power = Power(d)
    def kwh: Energy = Energy(d)

  // TODO: Will be more complex, an ADT (abstract data type) should be implemented
  object Tick:
    opaque type Tick = UnsignedLong
    def start: Tick = UnsignedLong.ZERO
    extension (tick: Tick)
      def next: Tick = tick.plus(UnsignedLong.ONE)

  case class GeographicPoint(latitude: Double, longitude: Double)

  // TODO: the following code will be necessary later...
  trait Coordinate[T]:
    def latitude(point: T): Double
    def longitude(point: T): Double

    extension (t: T)
      def lat: Double = latitude(t)
      def lon: Double = longitude(t)
      def distanceTo(other: T)(using Coordinate[T]): Double =
        val dLat = math.toRadians(latitude(other) - latitude(t))
        val dLon = math.toRadians(longitude(other) - longitude(t))
        val a = math.sin(dLat / 2) * math.sin(dLat / 2) +
          math.cos(math.toRadians(latitude(t))) *
            math.cos(math.toRadians(latitude(other))) *
            math.sin(dLon / 2) * math.sin(dLon / 2)
        6371 * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a)) // km

  given Coordinate[GeographicPoint] with
    def latitude(point: GeographicPoint): Double = point.latitude
    def longitude(point: GeographicPoint): Double = point.longitude
