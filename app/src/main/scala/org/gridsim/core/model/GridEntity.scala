package org.gridsim.core.model

/** Base trait for everything that has an identity in the grid. */
trait GridEntity:
  def id: String

trait GridEntityState:
  def entityId: String

abstract class GridEntityWithState[E <: GridEntity, S <: GridEntityState](entity: E, state: S)
