package org.gridsim.core.behaviour

import cats.data.State
import cats.implicits.*
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.Balanced
import org.gridsim.core.model.*
import org.gridsim.core.model.house.{House, HouseComponent}
import org.gridsim.core.model.battery.Battery

import scala.concurrent.duration.*

/**
 * A type class for resolving energy flows for a specific type T.
 *
 * Implementations of this trait define how an entity (like a House or a Battery)
 * interacts with an incoming energy flow and updates its internal state.
 *
 * @tparam T The type of entity being resolved.
 */
trait EnergyResolver[T]:
  /**
   * Resolves the energy balance for the given entity.
   *
   * @param flow The incoming energy flow (surplus or deficit).
   * @param env  The current environmental conditions.
   * @return A State transition that returns the residual energy flow after the entity has processed it.
   */
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
  given EnergyResolver[House] with
    def solve(flow: Flow[Energy], env: Environment): State[House, Flow[Energy]] =
      for {
        house <- State.get[House]

        internalResidue = ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)(using env.delta)
        initialResidue = internalResidue + flow

        (totalResidue, updatedComponents) = house.components.traverse { comp =>
          State[Flow[Energy], HouseComponent] { currentFlow =>
            val (newComp, nextResidue) = comp.solve(currentFlow, env).run(comp).value
            (nextResidue, newComp)
          }
        }.run(initialResidue).value

        _ <- State.modify[House](_.copy(components = updatedComponents))
      } yield totalResidue

  given EnergyResolver[Battery] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[Battery, Flow[Energy]] =
      BatteryBehaviour.update(residueEnergy)(using env.delta)

  given EnergyResolver[HouseComponent] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[HouseComponent, Flow[Energy]] =
      State { comp =>
        comp match
          case b: Battery =>
            val (newB, residue) = b.solve(residueEnergy, env).run(b).value
            (newB, residue)
      }

