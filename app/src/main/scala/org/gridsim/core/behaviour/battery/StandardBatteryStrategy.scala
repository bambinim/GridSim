package org.gridsim.core.behaviour.battery

import cats.data.State
import cats.syntax.all.*
import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.{Balanced, Deficit, Surplus}
import org.gridsim.core.common.Energy
import org.gridsim.core.model.battery.{BatterySpecification, BatteryState}

import scala.concurrent.duration.FiniteDuration

/**
 * Standard implementation of battery physical constraints.
 *
 * Respects maximum charging/discharging power, capacity limits, and Minimum State of Charge (SoC).
 */
object StandardBatteryStrategy extends BatteryStrategy:
  def charge(offered: Energy, spec: BatterySpecification)(using delta: FiniteDuration): State[BatteryState, Flow[Energy]] =
    for {
      state <- State.get[BatteryState]
      maxChargeable = spec.maxPowerCharge.toEnergy
      availableSpace = (spec.capacity - state.currentCharge).max(Energy.Zero)
      stored = offered.min(maxChargeable).min(availableSpace)
      residue = if (offered - stored) > Energy.Zero then Surplus(offered - stored) else Balanced
      _ <- State.modify[BatteryState](s => s.copy(currentCharge = s.currentCharge + stored))
    } yield residue

  def discharge(needed: Energy, spec: BatterySpecification)(using delta: FiniteDuration): State[BatteryState, Flow[Energy]] =
    for {
      state <- State.get[BatteryState]
      usable = (state.currentCharge - (spec.capacity * spec.minSoC)).max(Energy.Zero)
      maxDischargeable = spec.maxPowerDischarge.toEnergy
      discharged = needed.min(maxDischargeable).min(usable)
      residue = if (needed - discharged) > Energy.Zero then Deficit(needed - discharged) else Balanced
      _ <- State.modify[BatteryState](s => s.copy(currentCharge = s.currentCharge - discharged))
    } yield residue
