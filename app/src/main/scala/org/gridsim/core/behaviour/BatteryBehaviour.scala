package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.*
import org.gridsim.core.model.battery.{Battery, BatteryState}
import scala.concurrent.duration.FiniteDuration

object BatteryBehaviour:
  def update(battery: Battery, requestedPower: Power, delta: FiniteDuration): (BatteryState, Energy) =
    val state = battery.state
    val spec = battery.spec
    
    requestedPower match
      case p if p.toDouble > 0 =>
        val offeredEnergy = p.toEnergy(using delta)
        val maxAbsorbable = spec.maxPowerCharge.toEnergy(using delta)
        val energyAvailableRoom = spec.capacity - state.currentCharge
        
        val energyStored = Energy(
          offeredEnergy.toDouble
            .min(maxAbsorbable.toDouble)
            .min(energyAvailableRoom.toDouble)
        )
        
        val remainingExcess = offeredEnergy - energyStored
        (state.copy(currentCharge = state.currentCharge + energyStored), remainingExcess)
        
      case p if p.toDouble < 0 =>
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
        
        (state.copy(currentCharge = state.currentCharge - energyDischarged), remainingDeficit)

      case _ => (state, Energy.Zero)
