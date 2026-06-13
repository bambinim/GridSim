package org.gridsim.core.behaviour.house

import org.gridsim.core.common.Units.{Power, kw}
import org.gridsim.core.conf.{VacantConfig, TraditionalConfig, SmartWorkerConfig}
import pureconfig.ConfigSource
import org.gridsim.core.conf.given

/**
 * Defines the contract for an energy consumption strategy.
 * Implementations of this trait encapsulate the demand behavior of a house
 * based on its occupancy profile, allowing for pluggable and extensible
 * consumption modeling.
 */
trait ConsumptionStrategy:
  /**
   * Calculates the power demand for a given hour.
   *
   * @param h The hour of the day (0-23).
   * @return The instantaneous power demand.
   */
  def demandAt(h: Int): Power

/**
 * Traditional profile: represents residents with typical peak hours
 * in the early morning and evening.
 */
object TraditionalStrategy extends ConsumptionStrategy:
  private val cfg = ConfigSource.default.at("gridsim.consumption-profile.traditional").loadOrThrow[TraditionalConfig]
  override def demandAt(h: Int): Power = h match
    case h if (h >= cfg.morningPeakStart && h <= cfg.morningPeakEnd) || (h >= cfg.eveningPeakStart && h <= cfg.eveningPeakEnd) => cfg.peakPowerKw
    case _ => cfg.basePowerKw

/**
 * Smart Worker profile: represents residents working from home,
 * with sustained high demand throughout the day.
 */
object SmartWorkerStrategy extends ConsumptionStrategy:
  private val cfg = ConfigSource.default.at("gridsim.consumption-profile.smart-worker").loadOrThrow[SmartWorkerConfig]
  override def demandAt(h: Int): Power = h match
    case h if h >= cfg.workStart && h <= cfg.workEnd => cfg.workPowerKw
    case _ => cfg.basePowerKw

/**
 * Vacant profile: represents a house with minimal, constant baseline demand.
 */
object VacantStrategy extends ConsumptionStrategy:
  private val cfg = ConfigSource.default.at("gridsim.consumption-profile.vacant").loadOrThrow[VacantConfig]
  override def demandAt(h: Int): Power = cfg.basePowerKw
