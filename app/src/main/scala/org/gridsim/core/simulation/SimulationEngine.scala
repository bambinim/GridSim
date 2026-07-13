package org.gridsim.core.simulation

import scala.concurrent.duration.FiniteDuration

/**
 * Pure algebra for advancing a simulation by one discrete time step.
 *
 * An engine consumes an immutable [[SimulationState]] snapshot and produces the
 * next snapshot without mutating the input state or performing I/O. Scheduling,
 * pause/resume control and observer notification belong to the external
 * simulation runner.
 */
trait SimulationEngine:
  /**
   * Computes the state produced by one simulation tick.
   *
   * @param state current immutable simulation snapshot
   * @return the new snapshot after all phases of the tick have been resolved
   */
  def step(state: SimulationState, delta: FiniteDuration): SimulationState
