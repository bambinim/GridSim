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

  /**
   * Represents electrical power (kW).
   */
  opaque type Power = Double

  object Power:
    def apply(v: Double): Power = v
    val Zero: Power = 0.0

    given CommutativeMonoid[Power] = cats.instances.double.catsKernelStdGroupForDouble
    given Order[Power] = cats.instances.double.catsKernelStdOrderForDouble
    given Show[Power] = Show.show(p => String.format(Locale.US, "%.2f kW", p))

  extension (p: Power)
    @targetName("powerToDouble")
    def toDouble: Double = p
    @targetName("powerPlus")
    def +(o: Power): Power = p.toDouble + o.toDouble
    @targetName("powerMinus")
    def -(o: Power): Power = p.toDouble - o.toDouble
    @targetName("powerUnaryMinus")
    def unary_- : Power = -p.toDouble
    @targetName("powerTimes")
    def *(scalar: Double): Power = p.toDouble * scalar
    @targetName("powerDiv")
    def /(o: Power): Double = p.toDouble / o.toDouble
    @targetName("powerDivScalar")
    def /(scalar: Double): Power = p.toDouble / scalar
    def toEnergy(using tick: FiniteDuration): Energy =
      Energy(p.toDouble * tick.toUnit(TimeUnit.HOURS))
    @targetName("powerMin")
    def min(o: Power): Power = if p.toDouble <= o.toDouble then p else o
    @targetName("powerMax")
    def max(o: Power): Power = if p.toDouble >= o.toDouble then p else o
    @targetName("powerAbs")
    def abs: Power = p.toDouble.abs
    @targetName("powerLT")
    def <(o: Power): Boolean = p.toDouble < o.toDouble
    @targetName("powerGT")
    def >(o: Power): Boolean = p.toDouble > o.toDouble
    @targetName("powerLE")
    def <=(o: Power): Boolean = p.toDouble <= o.toDouble
    @targetName("powerGE")
    def >=(o: Power): Boolean = p.toDouble >= o.toDouble

  /**
   * Represents electrical energy (kWh).
   */
  opaque type Energy = Double

  object Energy:
    def apply(v: Double): Energy = v
    val Zero: Energy = 0.0

    given CommutativeMonoid[Energy] = cats.instances.double.catsKernelStdGroupForDouble
    given Order[Energy] = cats.instances.double.catsKernelStdOrderForDouble
    given Show[Energy] = Show.show(e => String.format(Locale.US, "%.2f kWh", e))

  extension (e: Energy)
    @targetName("energyToDouble")
    def toDouble: Double = e
    @targetName("energyPlus")
    def +(o: Energy): Energy = e.toDouble + o.toDouble
    @targetName("energyMinus")
    def -(o: Energy): Energy = e.toDouble - o.toDouble
    @targetName("energyUnaryMinus")
    def unary_- : Energy = -e.toDouble
    @targetName("energyTimes")
    def *(scalar: Double): Energy = e.toDouble * scalar
    @targetName("energyDiv")
    def /(o: Energy): Double = e.toDouble / o.toDouble
    @targetName("energyDivScalar")
    def /(scalar: Double): Energy = e.toDouble / scalar
    def toPower(using tick: FiniteDuration): Power =
      Power(e.toDouble / tick.toUnit(TimeUnit.HOURS))
    @targetName("energyMin")
    def min(o: Energy): Energy = if e.toDouble <= o.toDouble then e else o
    @targetName("energyMax")
    def max(o: Energy): Energy = if e.toDouble >= o.toDouble then e else o
    @targetName("energyAbs")
    def abs: Energy = e.toDouble.abs
    @targetName("energyLT")
    def <(o: Energy): Boolean = e.toDouble < o.toDouble
    @targetName("energyGT")
    def >(o: Energy): Boolean = e.toDouble > o.toDouble
    @targetName("energyLE")
    def <=(o: Energy): Boolean = e.toDouble <= o.toDouble
    @targetName("energyGE")
    def >=(o: Energy): Boolean = e.toDouble >= o.toDouble
    @targetName("toFlowEnergy")
    def toFlow: Flow[Energy] =
      if e > Energy.Zero then Flow.Surplus(e)
      else if e < Energy.Zero then Flow.Deficit(e.abs)
      else Flow.Balanced

  extension (d: Double)
    def kw: Power = Power(d)
    def kwh: Energy = Energy(d)

  /**
   * Represents an energy or power flow in the grid.
   * 
   * @tparam A The unit type of the flow (e.g., Energy or Power).
   */
  enum Flow[+A]:
    /** A surplus of energy/power available to be consumed or stored. */
    case Surplus(amount: A)
    /** A deficit of energy/power that needs to be supplied. */
    case Deficit(amount: A)
    /** A perfectly balanced state with no net flow. */
    case Balanced

  extension (f: Flow[Energy])
    /**
     * Returns a signed numeric representation of the flow.
     * Surplus is positive, Deficit is negative, Balanced is zero.
     */
    def value: Double = f match
      case Flow.Surplus(e) => e.toDouble
      case Flow.Deficit(e) => -e.toDouble
      case Flow.Balanced => 0.0

    /**
     * Combines two energy flows.
     */
    @targetName("combineFlows")
    def +(o: Flow[Energy]): Flow[Energy] =
      (f.value + o.value).kwh.toFlow

  object Tick:
    opaque type Tick = UnsignedLong
    def start: Tick = UnsignedLong.ZERO
    extension (tick: Tick)
      def next: Tick = tick.plus(UnsignedLong.ONE)

  case class GeographicPoint(latitude: Double, longitude: Double)

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
