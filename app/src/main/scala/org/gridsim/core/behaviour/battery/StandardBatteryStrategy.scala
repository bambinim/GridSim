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
  def charge(state: BatteryState, offered: Energy, spec: BatterySpecification)(using delta: FiniteDuration): (BatteryState, Flow[Energy]) =
    val maxChargeable = spec.maxPowerCharge.toEnergy
    val availableSpace = (spec.capacity - state.currentCharge).max(Energy.Zero)
    val stored = offered.min(maxChargeable).min(availableSpace)

    val residue = if ((offered - stored) > Energy.Zero) Surplus(offered - stored) else Balanced
    val nextState = state.copy(currentCharge = state.currentCharge + stored)

    (nextState, residue)


  def discharge(state: BatteryState, needed: Energy, spec: BatterySpecification)(using delta: FiniteDuration): (BatteryState, Flow[Energy]) =
    val usable = (state.currentCharge - (spec.capacity * spec.minSoC)).max(Energy.Zero)
    val maxDischargeable = spec.maxPowerDischarge.toEnergy
    val discharged = needed.min(maxDischargeable).min(usable)

    val residue = if ((needed - discharged) > Energy.Zero) Deficit(needed - discharged) else Balanced
    val nextState = state.copy(currentCharge = state.currentCharge - discharged)

    (nextState, residue)
    
