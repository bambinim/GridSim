package org.gridsim.core.behaviour.house

import org.gridsim.core.common.*
import org.gridsim.core.behaviour.{EvolutionContext, GridEvolution}
import org.gridsim.core.behaviour.storage.StorageEnergyExchanger.*
import org.gridsim.core.model.*
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.storage.{Storage, StorageState}

import scala.concurrent.duration.FiniteDuration

/**
 * Evolves a house for one simulation tick.
 *
 * The current flow order is:
 * 1. resolve the house consumption;
 * 2. combine it with component production;
 * 3. let storage components handle the resulting residue.
 *
 * Non-storage component evolution is not implemented yet, so production is
 * currently neutral. Future producer components should update `productionFlow`
 * before storage exchange runs.
 */
object HouseEvolution
    extends GridEvolution[HouseState, House, EvolutionContext[HouseEvolutionDependencies]]:

  extension (state: HouseState)
    /**
     * Evolves all house component states and returns the residual flow after storage exchange.
     */
    def evolve(house: House, environment: Environment)(
      using context: EvolutionContext[HouseEvolutionDependencies]
    ): (HouseState, Flow[Energy]) =
      val initialFlow = resolveConsumption(house, environment)
      val componentsById = house.components.map(component => component.id -> component).toMap

      val (updatedComponentStates, residualFlow) =
        evolveComponents(
          state.componentStates,
          componentsById,
          initialFlow,
          environment
        )(using context.delta)

      (state.copy(componentStates = updatedComponentStates), residualFlow)

  private def resolveConsumption(
    house: House,
    environment: Environment
  )(using context: EvolutionContext[HouseEvolutionDependencies]): Flow[Energy] =
    val dependencies = context.dependencies

    dependencies.resolver.resolve(environment.hourOfDay, house.strategy)(
      using context.delta,
      dependencies.shaper
    )

  private def evolveComponents(
    states: List[GridEntityState],
    componentsById: Map[String, GridEntity],
    initialFlow: Flow[Energy],
    environment: Environment
  )(using delta: FiniteDuration): (List[GridEntityState], Flow[Energy]) =
    val (residualFlow, reversedStates) =
      states.foldLeft((initialFlow, List.empty[GridEntityState])) {
        case ((currentFlow, updatedStates), currentState) =>
          val (nextState, nextFlow) =
            evolveComponent(
              currentState,
              componentsById.get(currentState.entityId),
              currentFlow,
              environment
            )

          (nextFlow, nextState :: updatedStates)
      }

    (reversedStates.reverse, residualFlow)

  private def evolveComponent(
    state: GridEntityState,
    entity: Option[GridEntity],
    flow: Flow[Energy],
    environment: Environment
  )(using delta: FiniteDuration): (GridEntityState, Flow[Energy]) =
    (state, entity) match
      case (storageState: StorageState, Some(storage: Storage)) =>
        storageState.exchange(storage, flow, environment)

      case _ =>
        (state, flow)
