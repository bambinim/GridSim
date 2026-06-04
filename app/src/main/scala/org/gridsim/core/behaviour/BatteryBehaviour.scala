package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.*
import org.gridsim.core.model.battery.{Battery, BatteryState}

import scala.concurrent.duration.FiniteDuration

object BatteryBehaviour:
  def update(battery: Battery, requested: Flow[Energy])(using delta: FiniteDuration): (Battery, Flow[Energy]) =
    val state = battery.state
    val spec = battery.spec
    
    requested match
      case Flow.Surplus(offeredEnergy) =>
        val maxAbsorbable = spec.maxPowerCharge.toEnergy
        val energyAvailableRoom = spec.capacity - state.currentCharge

        val energyStored = offeredEnergy.min(maxAbsorbable).min(energyAvailableRoom)

        val remainingExcess = offeredEnergy - energyStored
        val newState = state.copy(currentCharge = state.currentCharge + energyStored)

        val residue = if remainingExcess > Energy.Zero then Flow.Surplus(remainingExcess) else Flow.Balanced
        (battery.copy(state = newState), residue)

      case Flow.Deficit(neededEnergy) =>
        val maxDeliverable = spec.maxPowerDischarge.toEnergy

        val minCharge = spec.capacity * spec.minSoC
        val usableEnergy = (state.currentCharge - minCharge).max(Energy.Zero)

        val energyDischarged = neededEnergy.min(maxDeliverable).min(usableEnergy)

        val remainingDeficit = neededEnergy - energyDischarged
        val newState = state.copy(currentCharge = state.currentCharge - energyDischarged)

        val residue = if remainingDeficit > Energy.Zero then Flow.Deficit(remainingDeficit) else Flow.Balanced
        (battery.copy(state = newState), residue)

      case Flow.Balanced => (battery, Flow.Balanced)
