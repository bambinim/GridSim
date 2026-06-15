package org.gridsim.core.behaviour.battery

import cats.data.State
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.*
import org.gridsim.core.model.battery.{BatteryModel, BatterySpecification, BatteryState}

import scala.concurrent.duration.FiniteDuration

/**
 * Strategy pattern for battery energy processing.
 * Allows for different implementations of charging and discharging logic.
 */
trait BatteryStrategy:
  /**
   * Processes an incoming surplus of energy.
   *
   * @param offered The amount of energy available for charging.
   * @param spec    The physical specifications of the battery.
   * @return A State transition that updates the BatteryState and returns the residual flow.
   */
  def charge(offered: Energy, spec: BatterySpecification)(using delta: FiniteDuration): State[BatteryState, Flow[Energy]]

  /**
   * Processes an incoming deficit of energy.
   *
   * @param needed  The amount of energy requested from the battery.
   * @param spec    The physical specifications of the battery.
   * @return A State transition that updates the BatteryState and returns the residual flow.
   */
  def discharge(needed: Energy, spec: BatterySpecification)(using delta: FiniteDuration): State[BatteryState, Flow[Energy]]

object BatteryStrategy:
  /**
   * Dispatches the correct strategy based on the battery model.
   */
  def forModel(model: BatteryModel): BatteryStrategy = model match
    case BatteryModel.Standard => StandardBatteryStrategy
    case null                     => StandardBatteryStrategy

/**
 * Standard implementation of battery logic, respecting physical constraints.
 */
object StandardBatteryStrategy extends BatteryStrategy:
  override def charge(offered: Energy, spec: BatterySpecification)(using delta: FiniteDuration): State[BatteryState, Flow[Energy]] =
    for {
      state <- State.get[BatteryState]
      maxChargeable = spec.maxPowerCharge.toEnergy
      availableSpace = (spec.capacity - state.currentCharge).max(Energy.Zero)
      stored = offered.min(maxChargeable).min(availableSpace)
      residue = if (offered - stored) > Energy.Zero then Surplus(offered - stored) else Balanced
      _ <- State.modify[BatteryState](s => s.copy(currentCharge = s.currentCharge + stored))
    } yield residue

  override def discharge(needed: Energy, spec: BatterySpecification)(using delta: FiniteDuration): State[BatteryState, Flow[Energy]] =
    for {
      state <- State.get[BatteryState]
      usable = (state.currentCharge - (spec.capacity * spec.minSoC)).max(Energy.Zero)
      maxDischargeable = spec.maxPowerDischarge.toEnergy
      discharged = needed.min(maxDischargeable).min(usable)
      residue = if (needed - discharged) > Energy.Zero then Deficit(needed - discharged) else Balanced
      _ <- State.modify[BatteryState](s => s.copy(currentCharge = s.currentCharge - discharged))
    } yield residue
