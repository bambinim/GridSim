package org.gridsim.core.validation

import cats.data.{Validated, ValidatedNec}
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple3Semigroupal, catsSyntaxValidatedIdBinCompat0}
import org.gridsim.core.model.{GridEntity, GridEntityState}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.network.Cable
import org.gridsim.core.simulation.{SimulationModel, SimulationState}
import org.gridsim.core.validation.Validator.*

/**
 * Validates simulation-level coherence between an initial [[SimulationState]]
 * and the selected [[SimulationModel]].
 *
 * Component-specific validation is intentionally left to the DSL and to the
 * dedicated entity validators. This validator only checks invariants that
 * appear when already-created pieces are assembled into a runnable simulation.
 */
object SimulationValidator:
  /**
   * It accumulates all setup errors instead of failing fast.
   */
  given Validator[(SimulationState, SimulationModel)] with
    def validate(pair: (SimulationState, SimulationModel)): ValidatedNec[DomainError, (SimulationState, SimulationModel)] =
      val (state, model) = pair
      (
        validateState(state),
        validateStateAndModelCoherence(state, model)
      ).mapN((_, _) => pair)

  /** Validates simulation parameters owned by the state itself. */
  private def validateState(state: SimulationState): ValidatedNec[DomainError, Unit] =
    (
      state.delta.toNanos.toDouble.mustBePositive("Simulation Delta")
    ).map(_ => ())

  /**
   * Checks references from runtime state back to the static model.
   *
   * The state may contain entity states, previous entity flows, and previous
   * cable loads. Every one of those references must point to a node or cable
   * that exists in the model used by the simulation.
   */
  private def validateStateAndModelCoherence(state: SimulationState, model: SimulationModel): ValidatedNec[DomainError, Unit] =
    val nodeIds = model.grid.nodes.map(_.id).toSet
    val cables = model.grid.cables.toSet
    (
      validateEntityStatesMatchModelEntity(state, nodeIds),
      validateEntityFlows(state, nodeIds),
      validateCableLoads(state, cables)
    ).mapN((_, _, _) => ())

  /**
   * Ensures each entity state is stored under its own ID and that the ID exists
   * among the model nodes.
   */
  private def validateEntityStatesMatchModelEntity(state: SimulationState, nodeIds: Set[String]): ValidatedNec[DomainError, Unit] =
    state.entityStates.toList.foldLeft(().validNec[DomainError]){
      case (acc, (id, s)) =>
        val current = (
          requireValid(
            id == s.entityId,
            DomainError.EntityStateKeyMismatch(id, s.entityId)
          ),
          requireValid(
            nodeIds.contains(id),
            DomainError.EntityStateWithoutModel(id)
          )
        ).mapN((_, _) => ())
        (acc, current).mapN((_, _) => ())
    }

  /** Ensures every stored entity flow references a node from the selected model. */
  private def validateEntityFlows(state: SimulationState, nodeIds: Set[String]): ValidatedNec[DomainError, Unit] =
    state.entityFlows.toList.foldLeft(().validNec[DomainError]){
      case (acc, (id, _)) =>
        val current = (
          requireValid(
            nodeIds.contains(id),
            DomainError.EntityFlowWithoutModel(id)
          )
        ).map(_ => ())
        (acc, current).mapN((_, _) => ())
    }

  /** Ensures every stored cable load references a cable from the selected model. */
  private def validateCableLoads(state: SimulationState, cables: Set[Cable]): ValidatedNec[DomainError, Unit] =
    state.cableLoads.toList.foldLeft(().validNec[DomainError]){
      case (acc, (c, _)) =>
        val current = (
          requireValid(
            cables.contains(c),
            DomainError.CableLoadWithoutCable(c.connections.n1, c.connections.n2)
          )
          ).map(_ => ())
        (acc, current).mapN((_, _) => ())
    }
  
  private def requireValid(cond: Boolean, error: DomainError): ValidatedNec[DomainError, Unit] =
    Validated.condNec(
      cond,
      (),
      error
    )
