package org.gridsim.core.behaviour

import cats.data.State
import cats.Traverse
import cats.implicits.*
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.Balanced
import org.gridsim.core.model.*
import org.gridsim.core.model.house.House
import org.gridsim.core.model.battery.Battery
import org.gridsim.core.behaviour.battery.BatteryLogic.given
import org.gridsim.core.behaviour.house.ConsumptionProfile

import scala.concurrent.duration.*

/**
 * Defines the contract for resolving energy flows across domain entities.
 */
trait EnergyResolver[T]:
  def solve(flow: Flow[Energy], env: Environment): State[T, Flow[Energy]]

object EnergyResolver:
  extension [A](node: A)(using resolver: EnergyResolver[A])
    def solve(flow: Flow[Energy], env: Environment): State[A, Flow[Energy]] =
      resolver.solve(flow, env)

    def runSolve(flow: Flow[Energy], env: Environment): (A, Flow[Energy]) =
      resolver.solve(flow, env).run(node).value

    def solve(env: Environment): State[A, Flow[Energy]] =
      resolver.solve(Balanced, env)

    def runSolve(env: Environment): (A, Flow[Energy]) =
      resolver.solve(Balanced, env).run(node).value

  /**
   * Resolver instance for a [[House]].
   * Follows a multi-pass approach:
   * 1. Internal consumption is calculated.
   * 2. Producers are processed to cover demand or create surplus.
   * 3. Storages are processed to store surplus or cover remaining deficit.
   */
  given [F[_]: Traverse]: EnergyResolver[House[F]] with
    def solve(flow: Flow[Energy], env: Environment): State[House[F], Flow[Energy]] =
      for {
        house <- State.get[House[F]]
        internalFlow = ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)(using env.delta)
        initialResidue = internalFlow + flow

        (afterProducersResidue, updatedProducers) = house.producers.traverse { prod =>
          State[Flow[Energy], Producer] { currentFlow =>
            val (newProd, nextResidue) = prod.runSolve(currentFlow, env)
            (nextResidue, newProd)
          }
        }.run(initialResidue).value

        (afterStoragesResidue, updatedStorages) = house.storages.traverse { stor =>
          State[Flow[Energy], Storage] { currentFlow =>
            val (newStor, nextResidue) = stor.runSolve(currentFlow, env)
            (nextResidue, newStor)
          }
        }.run(afterProducersResidue).value

        _ <- State.modify[House[F]](_.copy(producers = updatedProducers, storages = updatedStorages))
      } yield afterStoragesResidue

  /**
   * Resolver instance for a [[Battery]].
   */
  given EnergyResolver[Battery] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[Battery, Flow[Energy]] =
      summon[EnergyLogic[Battery]].process(residueEnergy, env)

  /**
   * Dispatches the energy resolution to storage components.
   */
  given storageResolver: EnergyResolver[Storage] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[Storage, Flow[Energy]] =
      State {
        case b: Battery => b.runSolve(residueEnergy, env)
        case other      => (other, residueEnergy)
      }

  /**
   * Dispatches the energy resolution to producer components.
   */
  given producerResolver: EnergyResolver[Producer] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[Producer, Flow[Energy]] =
      State {
        // Here we will add cases for SolarPanel, WindTurbine, etc.
        case other => (other, residueEnergy)
      }
