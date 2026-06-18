package org.gridsim.core.behaviour.storage.battery

import org.gridsim.core.behaviour.storage.StorageStrategy
import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.gridsim.core.model.storage.battery.{Battery, BatteryModel, BatteryState}

import scala.concurrent.duration.FiniteDuration

/**
 * Strategy pattern for battery energy processing.
 *
 * Implementations define how a battery state evolves when subjected to
 * an energy surplus (charging) or deficit (discharging).
 */
trait BatteryStrategy extends StorageStrategy[BatteryState, Battery]:
  /**
   * Calculates the updated state and residual energy after a charging attempt.
   *
   * @param state   The current state of the battery (via extension).
   * @param offered The amount of energy available for charging.
   * @param spec    The physical specifications of the battery.
   * @param delta   The duration of the simulation tick.
   * @return A State transition for the [[BatteryState]].
   */
  extension (state: BatteryState)
    def charge(offered: Energy, b: Battery)(using delta: FiniteDuration): (BatteryState, Flow[Energy])

    /**
     * Calculates the updated state and residual energy after a discharging attempt.
     *
     * @param state  The current state of the battery (via extension).
     * @param needed The amount of energy requested from the battery.
     * @param spec   The physical specifications of the battery.
     * @param delta  The duration of the simulation tick.
     * @return A State transition for the [[BatteryState]].
     */
    def discharge(needed: Energy, b: Battery)(using delta: FiniteDuration): (BatteryState, Flow[Energy])

object BatteryStrategy:
  /**
   * Factory method to obtain the strategy associated with a specific [[BatteryModel]].
   */
  def forModel(model: BatteryModel): BatteryStrategy = model match
    case BatteryModel.Standard => StandardBatteryStrategy
    case null                     => StandardBatteryStrategy


