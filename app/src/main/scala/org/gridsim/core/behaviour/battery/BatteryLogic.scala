package org.gridsim.core.behaviour.battery

import cats.data.State
import org.gridsim.core.behaviour.{EnergyExchanger, EnergyResolver}
import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.gridsim.core.model.Environment
import org.gridsim.core.model.battery.{Battery, BatteryState}

import scala.concurrent.duration.FiniteDuration

/**
 * Logic implementation for the [[Battery]] entity.
 *
 * It bridges the domain model with the physical simulation by applying
 * battery-specific strategies to resolve incoming energy flows.
 */
object BatteryLogic:
  /**
   * Provides the [[EnergyExchanger]] implementation for the [[Battery]].
   *
   * It dispatches the incoming flow to the appropriate charging or discharging
   * strategy based on the battery model and specifications.
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
