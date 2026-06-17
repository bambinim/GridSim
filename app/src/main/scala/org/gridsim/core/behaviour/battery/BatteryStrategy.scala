package org.gridsim.core.behaviour.battery

import cats.data.State
import org.gridsim.core.behaviour.StorageStrategy
import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.gridsim.core.common.Energy
import org.gridsim.core.model.battery.{BatteryModel, BatterySpecification, BatteryState}

import scala.concurrent.duration.FiniteDuration

/**
 * Strategy pattern for battery energy processing.
 *
 * Implementations define how a battery state evolves when subjected to
 * an energy surplus (charging) or deficit (discharging).
 */
trait BatteryStrategy extends StorageStrategy[BatteryState, BatterySpecification]:
  /**
   * Calculates the updated state and residual energy after a charging attempt.
   *
   * @param state   The current state of the battery.
   * @param offered The amount of energy available for charging.
   * @param spec    The physical specifications of the battery.
   * @param delta   The duration of the simulation tick.
   * @return A State transition for the [[BatteryState]].
   */
  def charge(state: BatteryState, offered: Energy, spec: BatterySpecification)(using delta: FiniteDuration): (BatteryState, Flow[Energy])

  /**
   * Calculates the updated state and residual energy after a discharging attempt.
   *
   * @param state  The current state of the battery.
   * @param needed The amount of energy requested from the battery.
   * @param spec   The physical specifications of the battery.
   * @param delta  The duration of the simulation tick.
   * @return A State transition for the [[BatteryState]].
   */
  def discharge(state: BatteryState, needed: Energy, spec: BatterySpecification)(using delta: FiniteDuration): (BatteryState, Flow[Energy])

object BatteryStrategy:
  /**
   * Factory method to obtain the strategy associated with a specific [[BatteryModel]].
   */
  def forModel(model: BatteryModel): BatteryStrategy = model match
    case BatteryModel.Standard => StandardBatteryStrategy
    case null                     => StandardBatteryStrategy

  /**
   * Syntax extensions for BatteryState to allow fluent method calls.
   * Example: state.charge(offered, spec)
   */
  extension (s: BatteryState)
    def charge(offered: Energy, spec: BatterySpecification)(using delta: FiniteDuration, strategy: BatteryStrategy): (BatteryState, Flow[Energy]) =
      strategy.charge(s, offered, spec)

    def discharge(needed: Energy, spec: BatterySpecification)(using delta: FiniteDuration, strategy: BatteryStrategy): (BatteryState, Flow[Energy]) =
      strategy.discharge(s, needed, spec)


