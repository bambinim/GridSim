package org.gridsim.core.behaviour.house

import org.gridsim.core.behaviour.{EntityEvolutionHandler, EvolutionContext}
import org.gridsim.core.behaviour.house.HouseEvolution.evolve as evolveHouse
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.{Environment, GridEntity, GridEntityState}
import org.gridsim.core.model.house.{House, HouseState}

import scala.concurrent.duration.FiniteDuration

/**
 * Adapts [[HouseEvolution]] to the generic entity evolution dispatcher.
 *
 * @param dependencies services required to resolve house consumption and demand
 *                     shaping during a simulation tick
 */
final case class HouseEvolutionHandler(
  dependencies: HouseEvolutionDependencies
) extends EntityEvolutionHandler:

  override val stateClass: Class[HouseState] = classOf[HouseState]
  override val entityClass: Class[House] = classOf[House]

  /**
   * Evolves a house state-model pair by one simulation tick.
   */
  override def evolve(
    state: GridEntityState,
    entity: GridEntity,
    environment: Environment,
    delta: FiniteDuration
  ): (GridEntityState, Flow[Energy]) =
    given EvolutionContext[HouseEvolutionDependencies] =
      EvolutionContext(delta, dependencies)

    stateClass
      .cast(state)
      .evolveHouse(entityClass.cast(entity), environment)
