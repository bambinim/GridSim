package org.gridsim.model

import org.gridsim.common.Units.Tick.Tick
import org.gridsim.common.Units.Power
import org.gridsim.common.Units.GeographicPoint

// TODO: to verify if ok
case class WeatherConditions(
  irradiance: Power,  // kW/m²
  // windSpeed: Double,  // m/s
  temperature: Double // °C
)

trait Environment:
  def tick: Tick
  def hour: Int
  def irradiance(point: GeographicPoint): WeatherConditions
  def update(): Unit

