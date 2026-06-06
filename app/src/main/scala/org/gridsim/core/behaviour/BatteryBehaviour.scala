package org.gridsim.core.behaviour

import cats.data.State
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.{Balanced, Deficit, Surplus}
import org.gridsim.core.model.battery.{Battery, BatteryState}

import scala.concurrent.duration.FiniteDuration

object BatteryBehaviour:
  def update(requested: Flow[Energy])(using delta: FiniteDuration): State[Battery, Flow[Energy]] =
    requested match
      case Surplus(energy) => charge(energy)
      case Deficit(energy) => discharge(energy)
      case _ => State.pure(Balanced)

  private def charge(offeredEnergy: Energy)(using delta: FiniteDuration): State[Battery, Flow[Energy]] =
    for {
      battery <- State.get[Battery]

      state = battery.state
      spec = battery.spec

      maxAbsorbable = spec.maxPowerCharge.toEnergy
      energyAvailableRoom = spec.capacity - state.currentCharge

      energyStored = offeredEnergy.min(maxAbsorbable).min(energyAvailableRoom)

      remainingExcess = offeredEnergy - energyStored
      residue = if remainingExcess > Energy.Zero then Flow.Surplus(remainingExcess) else Flow.Balanced

      newState = state.copy(currentCharge = state.currentCharge + energyStored)

      _ <- State.modify[Battery](b => b.copy(state = newState))
    } yield residue

  private def discharge(neededEnergy: Energy)(using delta: FiniteDuration): State[Battery, Flow[Energy]] =
    for {
      battery <- State.get[Battery]

      state = battery.state
      spec = battery.spec

      maxDeliverable = spec.maxPowerDischarge.toEnergy

      minCharge = spec.capacity * spec.minSoC
      usableEnergy = (state.currentCharge - minCharge).max(Energy.Zero)

      energyDischarged = neededEnergy.min(maxDeliverable).min(usableEnergy)

      remainingDeficit = neededEnergy - energyDischarged
      residue = if remainingDeficit > Energy.Zero then Flow.Deficit(remainingDeficit) else Flow.Balanced

      newState = state.copy(currentCharge = state.currentCharge - energyDischarged)

      _ <- State.modify[Battery](b => b.copy(state = newState))
    } yield residue

