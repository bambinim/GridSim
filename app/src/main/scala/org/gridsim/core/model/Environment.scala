package org.gridsim.core.model

import org.gridsim.core.common.Temperatures.{TemperatureUnit, Temperature}
import org.gridsim.core.common.Units.{GeographicPoint, Power, Tick}

import scala.concurrent.duration.FiniteDuration

// TODO: to verify if ok
case class WeatherConditions(
  irradiance: Power, // kW/m²
  // windSpeed: Double, // m/s
  temperature: Temperature[TemperatureUnit]
)

trait Environment:
  def tick: Tick
  def hour: Int
  def delta: FiniteDuration
  def irradiance(point: GeographicPoint): WeatherConditions
  def update(): Unit
