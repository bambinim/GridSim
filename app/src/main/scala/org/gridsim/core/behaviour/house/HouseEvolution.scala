package org.gridsim.core.behaviour.house

import org.gridsim.core.behaviour.producer.SolarPanelEvolution.{evolve as evolveSolarPanel}
import org.gridsim.core.behaviour.storage.StorageEnergyExchanger.*
import org.gridsim.core.behaviour.{EvolutionContext, GridEvolution}
import org.gridsim.core.common.*
import org.gridsim.core.model.*
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.storage.{Storage, StorageState}

import scala.concurrent.duration.FiniteDuration

/**
 * Evolves a house for one simulation tick.
 *
 * The house-level flow order is:
 * 1. resolve base house consumption;
 * 2. evolve producer components and add their production;
 * 3. evolve storage components against the resulting residual flow.
 */
object HouseEvolution extends GridEvolution[HouseState, House, EvolutionContext[HouseEvolutionDependencies]]:

  extension (state: HouseState)
    /**
     * Evolves all house component states and returns the residual flow after
     * production and storage exchange have been resolved.
     */
    def evolve(house: House, environment: Environment)(
      using context: EvolutionContext[HouseEvolutionDependencies]
    ): (HouseState, Flow[Energy]) =
      val initialFlow = resolveConsumption(house, environment)
      val componentsById =
        house.components.map(component => component.id -> component).toMap

      val result =
        HouseComponentEvolution.evolveAll(
          states = state.componentStates,
          componentsById = componentsById,
          initialFlow = initialFlow,
          environment = environment
        )(using context.delta)

      (state.copy(componentStates = result.states), result.flow)

  private def resolveConsumption(
    house: House,
    environment: Environment
  )(using context: EvolutionContext[HouseEvolutionDependencies]): Flow[Energy] =
    val dependencies = context.dependencies

    dependencies.resolver.resolve(environment.hourOfDay, house.strategy)(
      using context.delta,
      dependencies.shaper
    )

private final case class ComponentEvolutionResult(
  states: List[GridEntityState],
  flow: Flow[Energy]
)

private object HouseComponentEvolution:

  private type ComponentEvolution =
    (GridEntityState, GridEntity, Flow[Energy], Environment, FiniteDuration) =>
      Option[(GridEntityState, Flow[Energy])]

  def evolveAll(
    states: Iterable[GridEntityState],
    componentsById: Map[String, GridEntity],
    initialFlow: Flow[Energy],
    environment: Environment
  )(using delta: FiniteDuration): ComponentEvolutionResult =
    val producerResult =
      evolvePhase(
        states,
        componentsById,
        initialFlow,
        environment
      )(evolveProducer)

    evolvePhase(
      producerResult.states,
      componentsById,
      producerResult.flow,
      environment
    )(evolveStorage)

  private def evolvePhase(
    states: Iterable[GridEntityState],
    componentsById: Map[String, GridEntity],
    initialFlow: Flow[Energy],
    environment: Environment
  )(
    evolve: ComponentEvolution
  )(using delta: FiniteDuration): ComponentEvolutionResult =
    val (flow, reversedStates) =
      states.foldLeft((initialFlow, List.empty[GridEntityState])) {
        case ((currentFlow, updatedStates), state) =>
          val (nextState, nextFlow) =
            componentsById
              .get(state.entityId)
              .flatMap(entity => evolve(state, entity, currentFlow, environment, delta))
              .getOrElse(state -> currentFlow)

          (nextFlow, nextState :: updatedStates)
      }

    ComponentEvolutionResult(reversedStates.reverse, flow)

  private def evolveProducer(
    state: GridEntityState,
    entity: GridEntity,
    flow: Flow[Energy],
    environment: Environment,
    delta: FiniteDuration
  ): Option[(GridEntityState, Flow[Energy])] =
    (state, entity) match
      case (panelState: SolarPanelState, panel: SolarPanel) =>
        given EvolutionContext[Unit] =
          EvolutionContext(delta, ())

        val (nextState, production) =
          panelState.evolveSolarPanel(panel, environment)

        Some(nextState -> (flow + production))

      case _ =>
        None

  private def evolveStorage(
    state: GridEntityState,
    entity: GridEntity,
    flow: Flow[Energy],
    environment: Environment,
    delta: FiniteDuration
  ): Option[(GridEntityState, Flow[Energy])] =
    (state, entity) match
      case (storageState: StorageState, storage: Storage) =>
        given FiniteDuration = delta

        Some(storageState.exchange(storage, flow, environment))

      case _ =>
        None
