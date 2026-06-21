package org.gridsim.core.model.storage

import org.gridsim.core.common.Energy
import org.gridsim.core.model.GridEntityState
import cats.implicits.catsSyntaxPartialOrder

trait StorageState extends GridEntityState:
  def currentCharge: Energy

object StorageState:
  /**
   * Extension methods providing utility calculations that require merging
   * the static entity topology with its dynamic state snapshot.
   */
  extension (storage: Storage)
    /**
     * Calculates the current fill level of the storage device.
     * Enforces that the provided state belongs to this exact storage entity.
     *
     * @param state The dynamic [[StorageState]] corresponding to this storage entity.
     * @return A [[Double]] value ranging from `0.0` (empty) to `1.0` (fully charged).
     * @throws IllegalArgumentException if the storage.id does not match the state.entityId
     */
    def percentage(state: StorageState): Double =
      require(
        storage.id == state.entityId,
        s"ID Mismatch: Impossible to calculate percentuage. Entity ID [${storage.id}] non corrisponde allo State ID [${state.entityId}]"
      )

      if storage.maxCapacity > Energy.Zero then
        (state.currentCharge / storage.maxCapacity)
      else
        0.0
