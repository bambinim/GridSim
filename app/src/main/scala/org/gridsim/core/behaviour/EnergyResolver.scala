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
   */
  given [F[_]: Traverse]: EnergyResolver[House[F]] with
    def solve(flow: Flow[Energy], env: Environment): State[House[F], Flow[Energy]] =
      for {
        house <- State.get[House[F]]
        internalFlow = ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)(using env.delta)
        initialResidue = internalFlow + flow

        (totalResidue, updatedComponents) = house.components.traverse { comp =>
          State[Flow[Energy], GridEntity & CanBeInHouse] { currentFlow =>
            val (newComp, nextResidue) = comp.runSolve(currentFlow, env)
            (nextResidue, newComp)
          }
        }.run(initialResidue).value

        _ <- State.modify[House[F]](_.copy(components = updatedComponents))
      } yield totalResidue

  /**
   * Resolver instance for a [[Battery]].
   */
  given EnergyResolver[Battery] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[Battery, Flow[Energy]] =
      summon[EnergyLogic[Battery]].process(residueEnergy, env)

  /**
   * Dispatches the energy resolution to house components.
   */
  given houseComponentResolver: EnergyResolver[GridEntity & CanBeInHouse] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[GridEntity & CanBeInHouse, Flow[Energy]] =
      State {
        case b: Battery => b.runSolve(residueEnergy, env)
        case other      => (other, residueEnergy)
      }
