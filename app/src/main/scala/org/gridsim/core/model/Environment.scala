package org.gridsim.core.model

import org.gridsim.core.common.Coordinates.GeographicPoint
import org.gridsim.core.common.Temperatures.{Temperature, TemperatureUnit}
import org.gridsim.core.common.Ticks.Tick
import org.gridsim.core.common.Units.Power

import scala.concurrent.duration.FiniteDuration

case class WeatherConditions(
  irradiance: Power, // TODO: maybe kW/m2?
  // windSpeed: Double, // m/s
  temperature: Temperature[TemperatureUnit]
)

trait Environment:
  def tick: Tick
  def hour: Int
  def delta: FiniteDuration
  def weather(point: GeographicPoint): WeatherConditions
  def update(): Unit
