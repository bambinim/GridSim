package org.gridsim.core.behaviour

import cats.data.State
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.Balanced
import org.gridsim.core.model.*
import org.gridsim.core.model.battery.Battery
import org.gridsim.core.behaviour.battery.BatteryLogic.given

import scala.concurrent.duration.FiniteDuration

/**
 * Defines the contract for resolving energy flows across domain entities.
 */
trait EnergyResolver[T]:
  def solve(flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): State[T, Flow[Energy]]

object EnergyResolver:
  extension [A](node: A)(using resolver: EnergyResolver[A])
    def solve(flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): State[A, Flow[Energy]] =
      resolver.solve(flow, env)

    def runSolve(flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): (A, Flow[Energy]) =
      resolver.solve(flow, env).run(node).value

    def solve(env: Environment)(using delta: FiniteDuration): State[A, Flow[Energy]] =
      resolver.solve(Balanced, env)

    def runSolve(env: Environment)(using delta: FiniteDuration): (A, Flow[Energy]) =
      resolver.solve(Balanced, env).run(node).value

  /**
   * Dispatches the energy resolution to storage components.
   */
  given storageResolver: EnergyResolver[Storage] with
    def solve(residueEnergy: Flow[Energy], env: Environment)(using delta: FiniteDuration): State[Storage, Flow[Energy]] =
      State {
        case b: Battery => b.runSolve(residueEnergy, env)
        case other      => (other, residueEnergy)
      }

  /**
   * Dispatches the energy resolution to producer components.
   */
  given producerResolver: EnergyResolver[Producer] with
    def solve(residueEnergy: Flow[Energy], env: Environment)(using delta: FiniteDuration): State[Producer, Flow[Energy]] =
      State {
        // Here we will add cases for SolarPanel, WindTurbine, etc.
        case other => (other, residueEnergy)
      }
