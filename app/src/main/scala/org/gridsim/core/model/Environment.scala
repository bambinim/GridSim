package org.gridsim.core.model

import org.gridsim.core.common.Coordinates.GeographicPoint
import org.gridsim.core.common.Temperatures.{AnyTemperature, Temperature, TemperatureUnit}
import org.gridsim.core.common.Ticks.Tick
import org.gridsim.core.common.Units.Power

import scala.concurrent.duration.FiniteDuration

case class WeatherConditions(
  irradiance: Power, // TODO: maybe kW/m2? Depends on the modelling of GeographicPoint!
  // windSpeed: Double, // m/s
  temperature: AnyTemperature
)

trait Environment:
  def tick: Tick
  def hour: Int
  def delta: FiniteDuration
  def weather(point: GeographicPoint): WeatherConditions
  def update(): Environment
