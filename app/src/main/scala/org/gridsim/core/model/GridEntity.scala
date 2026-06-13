package org.gridsim.core.model

/**
 * Base trait for everything that has an identity in the grid.
 */
trait GridEntity:
  def id: String

/**
 * Role trait for entities that can produce energy
 */
trait Producer extends GridEntity

/**
 * Role trait for entities that can store and release Energy.
 */
trait Storage extends GridEntity
