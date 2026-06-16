package org.gridsim.core.model

import org.gridsim.core.common.{GeographicPoint, Irradiance}
import org.gridsim.core.common.SimulationTime
import org.gridsim.core.common.Temperatures.AnyTemperature

import scala.concurrent.duration.FiniteDuration

/**
 * Weather conditions at a geographic location and at a given tick.
 *
 * @param irradiance incident solar irradiance (W/m²), 0 at night
 * @param temperature air temperature at the location
 */
case class WeatherConditions(
  irradiance: Irradiance,
  temperature: AnyTemperature
)

/**
 * Represents the state of the world outside the micro-grid at a given instant.
 *
 * The environment is immutable: each tick produces a new [[Environment]]
 * through [[advance]], without mutating the current state. Grid components
 * query the environment to calculate their production or consumption for the
 * current tick.
 */
trait Environment:
  /** The current simulation time instant. */
  def time: SimulationTime

  /**
   * Returns the weather conditions at the geographic location [[point]]
   * at the given [[time]].
   *
   * The location influences the result because irradiance and wind conditions
   * may vary across space (e.g. solar panels on a specific rooftop, a wind farm
   * located in a valley).
   *
   * @param point geographic position of the requesting component
   * @return localized weather conditions
   */
  def weather(point: GeographicPoint): WeatherConditions

  /**
   * Produces a new [[Environment]] advanced by one simulation tick.
   *
   * The tick duration is defined externally (e.g. as a [[FiniteDuration]]
   * in the simulation configuration) and is not part of the environment's
   * contract: each call to [[advance]] corresponds to a single simulation
   * step, regardless of its actual real-world duration.
   *
   * @return the environment state at the next tick
   */
  def advance(): Environment

  def delta: FiniteDuration // FIXME: to remove
