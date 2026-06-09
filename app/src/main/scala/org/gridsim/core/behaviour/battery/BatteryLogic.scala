package org.gridsim.core.behaviour.battery

import cats.data.State
import org.gridsim.core.behaviour.{BatteryStrategy, EnergyLogic}
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
  /**
   * Provides the [[EnergyLogic]] for the [[Battery]] entity.
   * Dispatch the incoming flow to the appropriate behaviour([[charge()]] or
   * [[discharge()]].
   * Return a [[State]] transition that updates the [[Battery]] instance and
   * calculates the residual [[Energy]].
   */
  given EnergyLogic[Battery] with
    def process(flow: Flow[Energy], env: Environment): State[Battery, Flow[Energy]] =
      for {
        b <- State.get[Battery]
        strategy = BatteryStrategy.forModel(b.model)
        action = flow match
          case Surplus(e) => strategy.charge(e, b.spec)(using env.delta)
          case Deficit(e) => strategy.discharge(e, b.spec)(using env.delta)
          case _ => State.pure(Balanced)

        (nextState, residue) = action.run(b.state).value
        _ <- State.modify[Battery](_.copy(state = nextState))
      } yield residue

