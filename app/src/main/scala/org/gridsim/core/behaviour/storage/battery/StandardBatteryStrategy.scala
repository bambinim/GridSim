package org.gridsim.core.behaviour.storage.battery

import cats.syntax.all.*
import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.{Balanced, Deficit, Surplus}
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}

import scala.concurrent.duration.FiniteDuration

/**
 * Standard implementation of battery physical constraints.
 *
 * Respects maximum charging/discharging power, capacity limits, and Minimum State of Charge (SoC).
 */
object StandardBatteryStrategy extends BatteryStrategy:
  extension (state: BatteryState)
    def charge(offered: Energy, b: Battery)(using delta: FiniteDuration): (BatteryState, Flow[Energy]) =
      val maxChargeable = b.maxPowerCharge.toEnergy
      val availableSpace = (b.maxCapacity - state.currentCharge).max(Energy.Zero)
      val stored = offered.min(maxChargeable).min(availableSpace)

      val residue = if ((offered - stored) > Energy.Zero) Surplus(offered - stored) else Balanced
      val nextState = state.copy(currentCharge = state.currentCharge + stored)

      (nextState, residue)


    def discharge(needed: Energy, b: Battery)(using delta: FiniteDuration): (BatteryState, Flow[Energy]) =
      val usable = (state.currentCharge - (b.maxCapacity * b.minSoC)).max(Energy.Zero)
      val maxDischargeable = b.maxPowerDischarge.toEnergy
      val discharged = needed.min(maxDischargeable).min(usable)

      val residue = if ((needed - discharged) > Energy.Zero) Deficit(needed - discharged) else Balanced
      val nextState = state.copy(currentCharge = state.currentCharge - discharged)

      (nextState, residue)
