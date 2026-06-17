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
 * @tparam T The type state of the orchestrator.
 * @tparam A The type of orchestrator
 */
trait EnergyResolver[T, A]:
  /**
   * Calculates the net energy exchange for the orchestrator.
   *
   * @param state        The State of the entity orchestrator.
   * @param orchestrator The entity orchestrating the internal flows.
   * @param env          The current environment context.
   * @param delta        The duration of the simulation tick.
   * @return A tuple containing the updated orchestrator state and the resulting net flow.
   */
  def resolve(state: T, orchestrator: A, env: Environment)(using delta: FiniteDuration): (T, Flow[Energy])

object EnergyResolver:
  /** Extension methods to allow syntax. */
  extension [T, A](state: T)(using resolver: EnergyResolver[T, A])
    def resolve(orchestrator: A, env: Environment)(using delta: FiniteDuration): (T, Flow[Energy]) =
      resolver.resolve(state, orchestrator, env)


