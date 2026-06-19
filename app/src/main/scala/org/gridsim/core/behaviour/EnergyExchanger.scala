package org.gridsim.core.behaviour

import org.gridsim.core.model.*
import org.gridsim.core.model.battery.{Battery, BatteryState}
import org.gridsim.core.common.*
import org.gridsim.core.model.{Producer, ProducerState}
import org.gridsim.core.behaviour.battery.BatteryLogic.given
import org.gridsim.core.model.{SolarPanel, SolarPanelState}
import org.gridsim.core.behaviour.SolarPanelLogic.given

import scala.concurrent.duration.FiniteDuration

/**
 * Defines the contract for components that exchange energy based on an external flow request.
 *
 * Exchangers are reactive entities (like Storages or Producers) that consume surplus
 * or supply deficit when requested by an orchestrator.
 *
 * @tparam T The state type of the component.
 * @tparam A The type of the component.
 */
trait EnergyExchanger[T, A]:
  /**
   * Processes an incoming energy flow.
   *
   * @param state     The component state to update
   * @param component The component model.
   * @param flow      The requested energy flow (Surplus or Deficit).
   * @param env       The current environment context.
   * @param delta     The duration of the simulation tick.
   * @return A tuple containing the updated component state and the residual flow.
   */
  def exchange(state: T, component: A, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (T, Flow[Energy])

object EnergyExchanger:
  /** Extension methods to allow syntax like `state.exchange(flow, env)`. */
  extension [T, A](state: T)(using exchanger: EnergyExchanger[T, A])
    def exchange(component: A, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (T, Flow[Energy]) =
      exchanger.exchange(state, component, flow, env)

  /** Dispatches the energy exchange to storage components by pattern matching on the entity type. */
  given storageExchanger: EnergyExchanger[StorageState, Storage] with
    def exchange(state: StorageState, storage: Storage, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (StorageState, Flow[Energy]) =
      (state, storage) match
        case (s: BatteryState, battery: Battery) => s.exchange(battery, flow, env)
        case (s, _) => (s, flow)

  /** Dispatches the energy exchange to producer components. */
  given producerExchanger: EnergyExchanger[ProducerState, Producer] with
    def exchange(state: ProducerState, producer: Producer, flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (ProducerState, Flow[Energy]) =
      (state, producer) match
        case (s: SolarPanelState, panel: SolarPanel) => s.exchange(panel, flow, env)
        case (s, _) => (s, flow)
