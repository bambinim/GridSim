package org.gridsim.core.behaviour.battery

import cats.data.State
import org.gridsim.core.behaviour.{EnergyExchanger, EnergyResolver}
import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.gridsim.core.model.Environment
import org.gridsim.core.model.battery.{Battery, BatteryState}
import BatteryStrategy.*

import scala.concurrent.duration.FiniteDuration

/**
 * Logic implementation for the [[Battery]] entity.
 *
 * It bridges the domain model with the physical simulation by applying
 * battery-specific strategies to resolve incoming energy flows.
 */
object BatteryLogic:
  /**
   * Implementation of [[EnergyExchanger]] for [[Battery]].
   *
   * This exchanger evaluates the incoming [[Flow]] against the battery's
   * [[BatteryState]] and physical specifications. It acts as a state
   * transition function:
   *
   * It dispatches the incoming flow to the appropriate charging or discharging
   * strategy based on the battery model and specifications.
   */
  given EnergyExchanger[BatteryState, Battery] with
    def exchange(state: BatteryState, b: Battery, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (BatteryState, Flow[Energy]) =
      val strategy = BatteryStrategy.forModel(b.model)

      flow match
        case Surplus(e) => state.charge(e, b.spec)(using delta, strategy)
        case Deficit(e) => state.discharge(e, b.spec)(using delta, strategy)
        case _ => (state, Balanced)
