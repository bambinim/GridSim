package org.gridsim.core.model

/**
 * Base trait for everything that has an identity in the grid.
 */
trait GridEntity:
  def id: String

/**
 * Role trait for entities that can be placed inside a house.
 */
trait CanBeInHouse

/**
 * Role trait for entities that can exist directly on the grid.
 */
trait CanBeStandalone
