package org.gridsim.core.behaviour.storage

import org.gridsim.core.behaviour.storage.battery.BatteryEnergyExchange.given
import org.gridsim.core.common.*
import org.gridsim.core.model.Environment
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.model.storage.{Storage, StorageState}

import scala.concurrent.duration.FiniteDuration

/**
 * Contract for storage components that receive a flow and return the unhandled residue.
 *
 * A storage exchange is different from component evolution: storage reacts to a
 * flow already produced by the rest of the system, charging from surplus or
 * discharging into a deficit.
 */
trait StorageEnergyExchanger[S <: StorageState, E <: Storage]:
  /**
   * Applies an incoming flow to a storage state.
   *
   * @return the updated storage state and any flow that storage could not absorb or provide.
   */
  def exchange(state: S, entity: E, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (S, Flow[Energy])

object StorageEnergyExchanger:

  extension (state: StorageState)
    def exchange(storage: Storage, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (StorageState, Flow[Energy]) =
      (state, storage) match
        case (s: BatteryState, b: Battery) => summon[StorageEnergyExchanger[BatteryState, Battery]].exchange(s, b, flow, env)
        case (s, _) => (s, flow)
