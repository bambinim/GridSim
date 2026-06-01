package org.gridsim.common

import com.google.common.primitives.UnsignedLong
import org.gridsim.common.Units.Energy.{Energy, fromKWh}
import org.gridsim.common.Units.Power.{Power, fromKW}

import scala.concurrent.duration.FiniteDuration

object Units:

  object Energy:
    // Total amount of energy used over time
    opaque type Energy = Double

    def fromKWh(energy: Double): Energy =
      require(energy >= 0)
      energy

    def fromPower(power: Power, duration: FiniteDuration): Energy = power * duration

    extension (energy: Energy)
      def /(duration: FiniteDuration): Power = fromKW(energy / duration.toHours)
      // NOTE: It is better to not expose internal implementation, instead define operators
      //def kWh: Double = energy

  object Power:
    // Instantaneous rate of energy use or production
    opaque type Power = Double

    def fromKW(power: Double): Power =
      require(power >= 0)
      power

    def fromEnergy(energy: Energy, duration: FiniteDuration): Power = energy / duration

    extension (power: Power)
      def *(duration: FiniteDuration): Energy = fromKWh(power * duration.toHours)
      // NOTE: It is better to not expose internal implementation, instead define operators
      //def kw: Double = power

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
