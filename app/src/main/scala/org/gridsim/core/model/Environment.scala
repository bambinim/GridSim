package org.gridsim.core.model

import org.gridsim.core.common.{GeographicPoint, Irradiance, wm2}
import org.gridsim.core.common.Temperatures.{AnyTemperature, Temperature}

import scala.concurrent.duration.FiniteDuration

/** Weather conditions at a geographic location and at a given tick. */
trait WeatherConditions:
  /** incident solar irradiance (W/m²), 0 at night */
  def irradiance: Irradiance
  /** air temperature at the location */
  def temperature: AnyTemperature

private final case class WeatherConditionsImpl(irradiance: Irradiance, temperature: AnyTemperature)
  extends WeatherConditions

object WeatherConditions:
  def apply(irradiance: Irradiance, temperature: AnyTemperature): WeatherConditions =
    WeatherConditionsImpl(irradiance, temperature)

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
  def time: FiniteDuration

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
   * @param delta the time to elapse to go to next environment
   * @return the environment state at the next tick
   */
  def advance(delta: FiniteDuration): Environment

private final case class SimpleEnvironment(time: FiniteDuration) extends Environment:
  /** Simple deterministic weather model (placeholder). */
  override def weather(point: GeographicPoint): WeatherConditions =
    val irradiance =
      if time.toHours >= 6 && time.toHours <= 18 then
        (800.0 + 200.0 * math.sin(time.toHours / 24.0 * math.Pi)).wm2
      else
        Irradiance.Zero

    val temperature =
      val base = 15.0
      val daily = math.sin(time.toHours / 24.0 * 2 * math.Pi) * 5
      val seasonal = math.sin(time.toDays / 365.0 * 2 * math.Pi) * 10
      Temperature.celsius(base + daily + seasonal).toAny

    WeatherConditions(irradiance, temperature)

  /** Advance simulation by one tick using external delta converted to minutes. */
  override def advance(delta: FiniteDuration): Environment =
    copy(time + delta)

object Environment:
  def apply(time: FiniteDuration): Environment =
    SimpleEnvironment(time)
