package org.gridsim.core.behaviour.storage

import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.storage.{Storage, StorageState}

import scala.concurrent.duration.FiniteDuration

/**
 * Base trait defining the contract for energy storage strategies.
 *
 * Implementations determine how specific storage technologies (batteries,
 * thermal stores, etc.) evolve their state based on energy flows.
 *
 * @tparam S    The specific type of [[StorageState]].
 * @tparam E The specific type of [[Storage]].
 */
trait StorageStrategy[S <: StorageState, E <: Storage]:
  /**
   * Calculates the state transition and residual energy after a charging attempt.
   *
   * @param state   The current state of the storage unit (via extension).
   * @param offered The amount of energy available for charging.
   * @param spec    The physical specifications of the storage unit.
   * @param delta   The duration of the simulation tick.
   * @return A tuple containing the updated [[StorageState]] and the residual [[Flow]].
   */
  extension (state: S)
    def charge(offered: Energy, e: E)(using delta: FiniteDuration): (S, Flow[Energy])

    /**
     * Calculates the state transition and residual energy after a discharging attempt.
     *
     * @param state  The current state of the storage unit (via extension).
     * @param needed The amount of energy requested from the storage.
     * @param spec   The physical specifications of the storage unit.
     * @param delta  The duration of the simulation tick.
     * @return A tuple containing the updated [[StorageState]] and the residual [[Flow]].
     */
    def discharge(needed: Energy, e: E)(using delta: FiniteDuration): (S, Flow[Energy])
