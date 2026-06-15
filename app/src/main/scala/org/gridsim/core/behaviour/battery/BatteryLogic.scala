package org.gridsim.core.behaviour.battery

import cats.data.State
import org.gridsim.core.behaviour.{EnergyExchanger, EnergyResolver}
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.*
import org.gridsim.core.model.Environment
import org.gridsim.core.model.battery.{Battery, BatteryState}

import scala.concurrent.duration.FiniteDuration

/**
 * Implementation of [[EnergyResolver]] for [[Battery]].
 * Encapsulates the physical constraints of charging and discharging.
 */
object BatteryLogic:
  /**
   * Provides the [[EnergyResolver]] for the [[Battery]] entity.
   * Dispatch the incoming flow to the appropriate strategy.
   * Return a [[State]] transition that updates the [[Battery]] instance and
   * calculates the residual [[Energy]].
   */
  given EnergyExchanger[Battery] with
    def exchange(b: Battery, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (Battery, Flow[Energy]) =
      val strategy = BatteryStrategy.forModel(b.model)
      
      val action: State[BatteryState, Flow[Energy]] = flow match
        case Surplus(e) => strategy.charge(e, b.spec)
        case Deficit(e) => strategy.discharge(e, b.spec)
        case _          => State.pure(Balanced)
      
      val (nextState, residue) = action.run(b.state).value
      
      (b.copy(state = nextState), residue)
