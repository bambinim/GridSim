package org.gridsim.core.behaviour

import cats.data.State
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.*
import org.gridsim.core.model.Environment
import org.gridsim.core.model.battery.Battery

import scala.concurrent.duration.FiniteDuration

/**
 * Implementation of [[EnergyLogic]] for [[Battery]].
 * Encapsulates the physical constraints of charging and discharging.
 */
object BatteryLogic:
  given EnergyLogic[Battery] with
    def process(flow: Flow[Energy], env: Environment): State[Battery, Flow[Energy]] =
      given delta: FiniteDuration = env.delta
      flow match
        case Surplus(energy) => charge(energy)
        case Deficit(energy) => discharge(energy)
        case _               => State.pure(Balanced)

    private def charge(offered: Energy)(using delta: FiniteDuration): State[Battery, Flow[Energy]] =
      for {
        b <- State.get[Battery]
        stored = offered.min(b.spec.maxPowerCharge.toEnergy).min(b.spec.capacity - b.state.currentCharge)
        residue = if (offered - stored) > Energy.Zero then Surplus(offered - stored) else Balanced
        _ <- State.modify[Battery](curr => curr.copy(state = curr.state.copy(currentCharge = curr.state.currentCharge + stored)))
      } yield residue

    private def discharge(needed: Energy)(using delta: FiniteDuration): State[Battery, Flow[Energy]] =
      for {
        b <- State.get[Battery]
        usable = (b.state.currentCharge - (b.spec.capacity * b.spec.minSoC)).max(Energy.Zero)
        discharged = needed.min(b.spec.maxPowerDischarge.toEnergy).min(usable)
        residue = if (needed - discharged) > Energy.Zero then Deficit(needed - discharged) else Balanced
        _ <- State.modify[Battery](curr => curr.copy(state = curr.state.copy(currentCharge = curr.state.currentCharge - discharged)))
      } yield residue
