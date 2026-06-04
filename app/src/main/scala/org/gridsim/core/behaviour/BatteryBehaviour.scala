package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.*
import org.gridsim.core.model.battery.{Battery, BatteryState}
import scala.concurrent.duration.FiniteDuration

object BatteryBehaviour:
  def update(battery: Battery, requestedPower: Power, delta: FiniteDuration): (Battery, Energy) =
    val state = battery.state
    val spec = battery.spec
    
    given FiniteDuration = delta

    if requestedPower > Power.Zero then
      val offeredEnergy = requestedPower.toEnergy
      val maxAbsorbable = spec.maxPowerCharge.toEnergy
      val energyAvailableRoom = spec.capacity - state.currentCharge

      val energyStored = offeredEnergy.min(maxAbsorbable).min(energyAvailableRoom)

      val remainingExcess = offeredEnergy - energyStored
      val newState = state.copy(currentCharge = state.currentCharge + energyStored)

      (battery.copy(state = newState), remainingExcess)

    else if requestedPower < Power.Zero then
      val neededEnergy = requestedPower.abs.toEnergy
      val maxDeliverable = spec.maxPowerDischarge.toEnergy

      val minCharge = spec.capacity * spec.minSoC
      val usableEnergy = (state.currentCharge - minCharge).max(Energy.Zero)

      val energyDischarged = neededEnergy.min(maxDeliverable).min(usableEnergy)

      val remainingDeficit = neededEnergy - energyDischarged
      val newState = state.copy(currentCharge = state.currentCharge - energyDischarged)

      (battery.copy(state = newState), -remainingDeficit)

    else (battery, Energy.Zero)
