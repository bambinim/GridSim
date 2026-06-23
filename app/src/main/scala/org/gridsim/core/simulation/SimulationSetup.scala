package org.gridsim.core.simulation

import cats.data.ValidatedNec
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator
import org.gridsim.core.validation.Validator.validate

/**
 * Entry point for assembling a simulation from an initial state and a static model.
 *
 * This object does not contain validation rules directly: it delegates to the
 * available [[Validator]] instance for the `(SimulationState, SimulationModel)`
 * pair, keeping setup construction separate from the validation policy.
 */
object SimulationSetup:
  /**
   * Validates and returns the simulation setup if the initial state is coherent
   * with the selected model.
   *
   * @param s initial dynamic simulation snapshot
   * @param m static simulation model and grid topology
   * @return accumulated domain errors, or the validated setup pair
   */
  def make(s: SimulationState, m: SimulationModel)(using Validator[(SimulationState, SimulationModel)]): ValidatedNec[DomainError, (SimulationState, SimulationModel)] =
    (s, m).validate
