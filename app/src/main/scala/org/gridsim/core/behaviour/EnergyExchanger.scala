package org.gridsim.core.behaviour

import org.gridsim.core.model.*
import org.gridsim.core.model.battery.{Battery, BatteryState}
import org.gridsim.core.common.*
import org.gridsim.core.behaviour.battery.BatteryLogic.given

import scala.concurrent.duration.FiniteDuration

/**
 * Defines the contract for components that exchange energy based on an external flow request.
 *
 * Exchangers are reactive entities (like Storages or Producers) that consume surplus
 * or supply deficit when requested by an orchestrator.
 *
 * @tparam T The type of the component.
 */
trait EnergyExchanger[T, A]:
  /**
   * Processes an incoming energy flow.
   *
   * @param component The component state to update.
   * @param flow      The requested energy flow (Surplus or Deficit).
   * @param env       The current environment context.
   * @param delta     The duration of the simulation tick.
   * @return A tuple containing the updated component state and the residual flow.
   */
  def exchange(state: T, component: A, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (T, Flow[Energy])

object EnergyExchanger:
  /** Extension methods to allow syntax like `component.exchange(flow, env)`. */
  extension [T, A](state: T)(using exchanger: EnergyExchanger[T, A])
    def exchange(component: A, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (T, Flow[Energy]) =
      exchanger.exchange(state, component, flow, env)

  /** Dispatches the energy exchange to storage components by pattern matching on the entity type. */
  given storageExchanger: EnergyExchanger[StorageState, Storage] with
    def exchange(state: StorageState, storage: Storage, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (StorageState, Flow[Energy]) =
      (state, storage) match
        case (s: BatteryState, b: Battery) => s.exchange(b, flow, env)
        case (s, other) =>
          (s, flow)

  /** Dispatches the energy exchange to producer components. */
  /*given producerExchanger: EnergyExchanger[Producer] with
    def exchange(producer: Producer, residueEnergy: Flow[Energy], env: Environment)(using delta: FiniteDuration): (Producer, Flow[Energy]) =
      producer match
        // case p: SolarPanel => p.exchange(residueEnergy, env)
        case other => (other, residueEnergy)*/
