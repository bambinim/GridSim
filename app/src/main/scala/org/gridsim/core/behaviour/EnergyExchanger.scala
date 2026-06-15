package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.*
import org.gridsim.core.model.*
import org.gridsim.core.model.battery.Battery

import org.gridsim.core.behaviour.battery.BatteryLogic.given

import scala.concurrent.duration.FiniteDuration

/**
 * Defines the contract for components that exchange energy with a given flow.
 * Used for specific entities like Storages or Producers.
 */
trait EnergyExchanger[T]:
  def exchange(component: T, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (T, Flow[Energy])

object EnergyExchanger:
  extension [A](node: A)(using exchanger: EnergyExchanger[A])
    def exchange(flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (A, Flow[Energy]) =
      exchanger.exchange(node, flow, env)
  /**
   * Dispatches the energy exchange to storage components.
   */
  given storageExchanger: EnergyExchanger[Storage] with
    def exchange(storage: Storage, residueEnergy: Flow[Energy], env: Environment)(using delta: FiniteDuration): (Storage, Flow[Energy]) =
      storage match
        case b: Battery => b.exchange(residueEnergy, env)
        case other      => (other, residueEnergy)

  /**
   * Dispatches the energy exchange to producer components.
   */
  given producerExchanger: EnergyExchanger[Producer] with
    def exchange(producer: Producer, residueEnergy: Flow[Energy], env: Environment)(using delta: FiniteDuration): (Producer, Flow[Energy]) =
      producer match
        // case p: SolarPanel => p.exchange(residueEnergy, env)
        case other => (other, residueEnergy)
