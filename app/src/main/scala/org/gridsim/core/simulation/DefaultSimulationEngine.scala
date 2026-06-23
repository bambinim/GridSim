package org.gridsim.core.simulation

import org.gridsim.core.behaviour.EntityEvolutionDispatcher
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.{GridEntity, GridEntityState}
import org.gridsim.core.solver.PowerFlowSolver
import org.gridsim.core.model.Environment

import cats.data.State
/**
 * Default pure implementation of [[SimulationEngine]].
 *
 * A tick is evaluated in dependency order:
 *  1. advance the simulated environment by [[SimulationModel.delta]];
 *  2. evolve every entity-state pair through [[EntityEvolutionDispatcher]];
 *  3. collect the updated entity states and their residual energy flows;
 *  4. calculate cable loads through the configured [[PowerFlowSolver]].
 *
 * @param model immutable grid topology and duration represented by one tick
 * @param entityDispatcher dispatcher used to evolve each supported entity type
 * @param flowSolver strategy used to calculate loads on the grid cables
 */
final case class DefaultSimulationEngine(
  model: SimulationModel,
  flowSolver: PowerFlowSolver
)(using entityDispatcher: EntityEvolutionDispatcher) extends SimulationEngine:

  /**
   * Advances the supplied snapshot by one complete simulation tick.
   *
   * The returned value is a new immutable snapshot. The input state is left
   * unchanged.
   *
   * @param state snapshot at the beginning of the tick
   * @return snapshot containing the advanced environment, evolved entities,
   *         entity flows and cable loads
   */
  override def step(state: SimulationState): SimulationState =
    simulationPipeline.run(state).value._1

  private def simulationPipeline: State[SimulationState, Unit] =
    for {
      _ <- advanceEnvironment
      _ <- evolveEntities
      _ <- calculateCableLoads
    } yield()

  private def advanceEnvironment: State[SimulationState, Unit] = State.modify {
    s => s.copy(environment = s.environment.advance(model.delta))
  }

  private def evolveEntities: State[SimulationState, Unit] = State.modify { s =>
    val resolved = resolveEntities(s.entityStates, model.grid.nodes, s.environment)

    s.copy(
      entityStates = resolved.map(pair => pair._1.entityId -> pair._1).toMap,
      entityFlows = resolved.map(pair => pair._1.entityId -> pair._2).toMap
    )
  }

  private def calculateCableLoads: State[SimulationState, Unit] = State.modify {
    s => s.copy(cableLoads = flowSolver.solve(s.entityFlows).toMap)
  }

  /**
   * Evolves all dynamic entity states using their matching static models.
   *
   * @param entityStates dynamic entity states from the current snapshot
   * @param entityModels static entities configured in the grid topology
   * @param environment environment already advanced to the new tick
   * @return one updated state and residual energy flow for each input state
   */
  private def resolveEntities(
    entityStates: Map[String, GridEntityState],
    entityModels: Iterable[GridEntity],
    environment: Environment
  ): Iterable[(GridEntityState, Flow[Energy])] =
    pairEntities(entityStates, entityModels).map {
      case (entityState, entityModel) =>
        entityDispatcher.evolve(
          entityState,
          entityModel,
          environment,
          model.delta
        )
    }

  /**
   * Associates each dynamic state with its static model by entity identifier.
   *
   * Models without dynamic state, such as the external grid, are not included
   * in the returned collection.
   *
   * @param states dynamic states to associate
   * @param entities available static entity models
   * @return state-model pairs preserving the iteration order of `states`
   * @throws IllegalArgumentException if a state has no model with the same ID
   */
  private def pairEntities(
    states: Map[String, GridEntityState],
    entities: Iterable[GridEntity]
  ): Iterable[(GridEntityState, GridEntity)] =
    val modelsById =
      entities.map(entity => entity.id -> entity).toMap

    states.values.map { state =>
      val model = modelsById.getOrElse(
        state.entityId,
        throw IllegalArgumentException(
          s"Missing model for state '${state.entityId}'"
        )
      )
      state -> model
    }
