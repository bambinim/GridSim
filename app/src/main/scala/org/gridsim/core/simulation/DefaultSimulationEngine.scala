package org.gridsim.core.simulation

import org.gridsim.core.behaviour.EntityEvolutionDispatcher
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.{GridEntity, GridEntityState}
import org.gridsim.core.solver.PowerFlowSolver

final case class DefaultSimulationEngine(
  model: SimulationModel
)(using entityDispatcher: EntityEvolutionDispatcher,
  flowSolver: PowerFlowSolver) extends SimulationEngine:

  override def step(state: SimulationState): SimulationState =
    val newEnv = state.environment.advance(model.delta)
    val resolvedEntities =
      resolveEntities(state.entityStates, model.grid.nodes, newEnv)
    val newEntityStates = resolvedEntities.map((s, f) => s)
    val newEntityFlows = resolvedEntities.map((s, f) => s.entityId -> f).toMap
    val newCableLoads = flowSolver.solve(newEntityFlows).toMap

    state.copy(
      environment = newEnv,
      entityStates = newEntityStates,
      entityFlows = newEntityFlows,
      cableLoads = newCableLoads
    )

  private def resolveEntities(
    entityStates: Iterable[GridEntityState],
    entityModels: Iterable[GridEntity],
    environment: org.gridsim.core.model.Environment
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

  private def pairEntities(
    states: Iterable[GridEntityState],
    entities: Iterable[GridEntity]
  ): Iterable[(GridEntityState, GridEntity)] =
    val modelsById =
      entities.map(entity => entity.id -> entity).toMap

    states.map { state =>
      val model = modelsById.getOrElse(
        state.entityId,
        throw IllegalArgumentException(
          s"Missing model for state '${state.entityId}'"
        )
      )
      state -> model
    }
