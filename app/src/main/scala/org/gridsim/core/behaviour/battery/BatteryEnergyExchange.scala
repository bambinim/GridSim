package org.gridsim.core.behaviour.battery

import org.gridsim.core.behaviour.StorageEnergyExchanger
import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.gridsim.core.model.Environment
import org.gridsim.core.model.battery.{Battery, BatteryState}
import org.gridsim.core.behaviour.battery.BatteryStrategy.*

import scala.concurrent.duration.FiniteDuration

/**
 * Storage exchange implementation for batteries.
 *
 * Batteries charge from surplus flow and discharge into deficit flow according
 * to the strategy selected by their configured model.
 */
object BatteryEnergyExchange:
  given StorageEnergyExchanger[BatteryState, Battery] with
    def exchange(state: BatteryState, b: Battery, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (BatteryState, Flow[Energy]) =
      val strategy = BatteryStrategy.forModel(b.model)

      flow match
        case Surplus(e) => strategy.charge(state)(e, b)
        case Deficit(e) => strategy.discharge(state)(e, b)
        case _ => (state, Balanced)
