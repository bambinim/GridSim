package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.*
import org.gridsim.core.model.battery.{Battery, BatteryState}
import scala.concurrent.duration.FiniteDuration

object BatteryBehaviour:
  def update(battery: Battery, requestedPower: Power, delta: FiniteDuration): (Battery, Energy) =
    val state = battery.state
    val spec = battery.spec
    
    val req = requestedPower.toDouble

    req match
      case p if p > 0 =>
        val offeredEnergy = Power(p).toEnergy(using delta)
        val maxAbsorbable = spec.maxPowerCharge.toEnergy(using delta)
        val energyAvailableRoom = spec.capacity - state.currentCharge

        val energyStored = Energy(
          offeredEnergy.toDouble
            .min(maxAbsorbable.toDouble)
            .min(energyAvailableRoom.toDouble)
        )

        val remainingExcess = offeredEnergy - energyStored
        val newState = state.copy(currentCharge = state.currentCharge + energyStored)

        (battery.copy(state = newState), remainingExcess)

      case p if p < 0 =>
        val neededEnergy = Power(p.toDouble.abs).toEnergy(using delta)
        val maxDeliverable = spec.maxPowerDischarge.toEnergy(using delta)

        val minCharge = spec.capacity * spec.minSoC
        val usableEnergy = (state.currentCharge - minCharge).toDouble.max(0.0).kwh

        val energyDischarged = Energy(
          neededEnergy.toDouble
            .min(maxDeliverable.toDouble)
            .min(usableEnergy.toDouble)
        )

        val remainingDeficit = neededEnergy - energyDischarged
        val newState = state.copy(currentCharge = state.currentCharge - energyDischarged)

        (battery.copy(state = newState), remainingDeficit)

      case _ => (battery, Energy.Zero)
