package org.gridsim.core.behaviour.house

import org.gridsim.core.common.Units.{Power, kw}

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
  override def demandAt(h: Int): Power = h match
    case h if (h >= 7 && h <= 9) || (h >= 18 && h <= 22) => 8.0.kw
    case _ => 2.0.kw

/**
 * Smart Worker profile: represents residents working from home,
 * with sustained high demand throughout the day.
 */
object SmartWorkerStrategy extends ConsumptionStrategy:
  override def demandAt(h: Int): Power = h match
    case h if h >= 8 && h <= 23 => 5.0.kw
    case _ => 2.0.kw

/**
 * Vacant profile: represents a house with minimal, constant baseline demand.
 */
object VacantStrategy extends ConsumptionStrategy:
  override def demandAt(h: Int): Power = 1.0.kw