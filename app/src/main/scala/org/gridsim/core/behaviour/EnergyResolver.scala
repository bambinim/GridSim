package org.gridsim.core.behaviour

import cats.data.{State, ValidatedNec}
import cats.Traverse
import cats.implicits.*
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.Balanced
import org.gridsim.core.model.*
import org.gridsim.core.model.house.{House, HouseComponent}
import org.gridsim.core.model.battery.Battery

import scala.concurrent.duration.*

/**
 * Defines the contract for resolving energy flows across domain entities.
 *
 * An [[EnergyResolver]] encapsulates the physics of energy propagation:
 * - It takes an input energy flow (surplus or deficit).
 * - It evaluates local consumption/production based on the [[Environment]].
 * - It propagates the residual flow through nested components.
 * - It returns a [[State]] transition, effectively treating the entity as a state machine.
 *
 * @tparam T The type of entity (e.g., House, Battery).
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
  /**
   * Provides syntax for solving energy flows.
   */
  extension [A](node: A)(using resolver: EnergyResolver[A])
    /**
     * Resolves the energy flow and return the state transition.
     */
    def solve(flow: Flow[Energy], env: Environment): State[A, Flow[Energy]] =
      resolver.solve(flow, env)
    /**
     * Executes the resolution and return the updated entity and the resulting Residue
     */
    def runSolve(flow: Flow[Energy], env: Environment): (A, Flow[Energy]) =
      resolver.solve(flow, env).run(node).value
    /**
     * Resolves the flow assuming a [[Balanced]] (zero) flow.
     */
    def solve(env: Environment): State[A, Flow[Energy]] =
      resolver.solve(Balanced, env)
    /**
     * Executes the resolution assuming a [[Balanced]] input.
     */
    def runSolve(env: Environment): (A, Flow[Energy]) =
      resolver.solve(Balanced, env).run(node).value

  /**
   * Resolver instance for a [[House]].
   * Logic flow:
   * 1. Calculate internal base consupmtion based on house attributes.
   * 2. Accumulate this consumption with the external input flow.
   * 3. Use [[Traverse]] to propagate the residue sequentially through all house components.
   * 4. Update the house with the new component configuration.
   */
  given [F[_]: Traverse]: EnergyResolver[House[F]] with
    def solve(flow: Flow[Energy], env: Environment): State[House[F], Flow[Energy]] =
      for {
        house <- State.get[House[F]]

        internalResidue = ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)(using env.delta)
        initialResidue = internalResidue + flow

        (totalResidue, updatedComponents) = house.components.traverse { comp =>
          State[Flow[Energy], HouseComponent] { currentFlow =>
            val (newComp, nextResidue) = comp.solve(currentFlow, env).run(comp).value
            (nextResidue, newComp)
          }
        }.run(initialResidue).value

        _ <- State.modify[House[F]](_.copy(components = updatedComponents))
      } yield totalResidue

  /**
   * Resolver instance for a [[Battery]].
   * Delegates specific physical behaviour to [[BatteryBehaviour]].
   */
  given EnergyResolver[Battery] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[Battery, Flow[Energy]] =
      BatteryBehaviour.update(residueEnergy)(using env.delta)

  /**
   * Dispatches the energy resolution to the concrete implementation of the component.
   *
   * @dev Maintance Note:
   * When introducing a new type of [[HouseComponent]] you
   * must add the new component case to dispatch to the correct implementation.
   */
  given EnergyResolver[HouseComponent] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[HouseComponent, Flow[Energy]] =
      State { comp =>
        comp match
          case b: Battery =>
            val (newB, residue) = b.solve(residueEnergy, env).run(b).value
            (newB, residue)
      }

