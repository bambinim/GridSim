package org.gridsim.core.model

import org.gridsim.core.common.Units.Tick.Tick
import org.gridsim.core.common.Units.Power
import org.gridsim.core.common.Units.GeographicPoint

import scala.concurrent.duration.FiniteDuration

// TODO: to verify if ok
case class WeatherConditions(
  irradiance: Power,  // kW/m²
  // windSpeed: Double,  // m/s
  temperature: Double // °C
)

trait Environment:
  def tick: Tick
  def hour: Int
  def delta: FiniteDuration
  def irradiance(point: GeographicPoint): WeatherConditions
  def update(): Unit

