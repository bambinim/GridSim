package org.gridsim.core.behaviour

import cats.data.State
import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.Balanced
import org.gridsim.core.model.*
import org.gridsim.core.model.battery.Battery
import org.gridsim.core.behaviour.battery.BatteryLogic.given

import scala.concurrent.duration.FiniteDuration

/**
 * Defines the contract for resolving energy flows across complex domain entities.
 *
 * Resolvers are intended for orchestrators (like House) that manage internal
 * consumption and component interactions to produce a net energy flow.
 *
 * @tparam T The type of the orchestrator.
 */
trait EnergyResolver[T]:
  /**
   * Calculates the net energy exchange for the orchestrator.
   *
   * @param orchestrator The entity orchestrating the internal flows.
   * @param env          The current environment context.
   * @param delta        The duration of the simulation tick.
   * @return A tuple containing the updated orchestrator state and the resulting net flow.
   */
  def resolve(orchestrator: T, env: Environment)(using delta: FiniteDuration): (T, Flow[Energy])

object EnergyResolver:
  /** Extension methods to allow syntax like `house.resolve(env)`. */
  extension [A](node: A)(using resolver: EnergyResolver[A])
    def resolve(env: Environment)(using delta: FiniteDuration): (A, Flow[Energy]) =
      resolver.resolve(node, env)

