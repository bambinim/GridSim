package org.gridsim.core.behaviour

import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.*

import scala.concurrent.duration.FiniteDuration

/**
 * Dispatches a generic entity-state pair to its concrete evolution.
 *
 * The simulation engine depends only on this algebra and does not need to know
 * the concrete entity families supported by the application.
 */
trait EntityEvolutionDispatcher:
  def evolve(
    state: GridEntityState,
    entity: GridEntity,
    environment: Environment,
    delta: FiniteDuration
  ): (GridEntityState, Flow[Energy])

/**
 * Handles the evolution of one supported entity-state family.
 *
 * Implementations belong next to the concrete entity behaviour they adapt. This
 * keeps the central dispatcher closed to changes when a new entity family is
 * added: new code registers another handler instead of modifying dispatch
 * logic.
 */
trait EntityEvolutionHandler:

  /**
   * Runtime state type accepted by this handler.
   */
  def stateClass: Class[_ <: GridEntityState]

  /**
   * Runtime entity model type accepted by this handler.
   */
  def entityClass: Class[_ <: GridEntity]

  /**
   * Returns whether this handler can evolve the supplied state-model pair.
   */
  def supports(state: GridEntityState, entity: GridEntity): Boolean =
    stateClass.isAssignableFrom(state.getClass) &&
      entityClass.isAssignableFrom(entity.getClass)

  /**
   * Evolves the supplied state-model pair by one simulation tick.
   */
  def evolve(
    state: GridEntityState,
    entity: GridEntity,
    environment: Environment,
    delta: FiniteDuration
  ): (GridEntityState, Flow[Energy])

/**
 * Ordered collection of entity evolution handlers available to a dispatcher.
 *
 * Handler order is deterministic. If multiple handlers support the same pair,
 * the first one in this collection wins.
 */
final case class EntityEvolutionHandlers(values: List[EntityEvolutionHandler])

object EntityEvolutionDispatcher:

  /**
   * Builds a dispatcher from an explicit ordered handler collection.
   */
  def fromHandlers(handlers: EntityEvolutionHandler*): EntityEvolutionDispatcher =
    DefaultEntityEvolutionDispatcher(handlers.toList)

  /**
   * Provides a dispatcher whenever the application composition has supplied the
   * available evolution handlers.
   */
  given fromRegisteredHandlers(using
    handlers: EntityEvolutionHandlers
  ): EntityEvolutionDispatcher =
    DefaultEntityEvolutionDispatcher(handlers.values)

/**
 * Registry-backed dispatcher that delegates each pair to the first compatible
 * evolution handler.
 *
 * The dispatcher depends only on the [[EntityEvolutionHandler]] abstraction; it
 * has no knowledge of concrete entity families.
 */
final case class DefaultEntityEvolutionDispatcher(
  handlers: List[EntityEvolutionHandler]
) extends EntityEvolutionDispatcher:

  override def evolve(
    state: GridEntityState,
    entity: GridEntity,
    environment: Environment,
    delta: FiniteDuration
  ): (GridEntityState, Flow[Energy]) =
    handlers
      .find(_.supports(state, entity))
      .map(_.evolve(state, entity, environment, delta))
      .getOrElse {
        throw IllegalArgumentException(
          s"Unsupported entity-state pair: model '${entity.id}', state '${state.entityId}'"
        )
      }
