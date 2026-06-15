package org.gridsim.core.behaviour.battery

import cats.data.State
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.*
import org.gridsim.core.model.battery.{BatteryModel, BatterySpecification, BatteryState}

import scala.concurrent.duration.FiniteDuration

/**
 * Strategy pattern for battery energy processing.
 *
 * Implementations define how a battery state evolves when subjected to
 * an energy surplus (charging) or deficit (discharging).
 */
trait BatteryStrategy:
  /**
   * Calculates the updated state and residual energy after a charging attempt.
   *
   * @param offered The amount of energy available for charging.
   * @param spec    The physical specifications of the battery.
   * @param delta   The duration of the simulation tick.
   * @return A State transition for the [[BatteryState]].
   */
  def charge(offered: Energy, spec: BatterySpecification)(using delta: FiniteDuration): State[BatteryState, Flow[Energy]]

  /**
   * Calculates the updated state and residual energy after a discharging attempt.
   *
   * @param needed  The amount of energy requested from the battery.
   * @param spec    The physical specifications of the battery.
   * @param delta   The duration of the simulation tick.
   * @return A State transition for the [[BatteryState]].
   */
  def discharge(needed: Energy, spec: BatterySpecification)(using delta: FiniteDuration): State[BatteryState, Flow[Energy]]

object BatteryStrategy:
  /**
   * Factory method to obtain the strategy associated with a specific [[BatteryModel]].
   */
  def forModel(model: BatteryModel): BatteryStrategy = model match
    case BatteryModel.Standard => StandardBatteryStrategy
    case null                     => StandardBatteryStrategy


