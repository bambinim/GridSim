package org.gridsim.core.behaviour

import org.gridsim.core.model.{Environment, GridEntityState, GridEntity}
import org.gridsim.core.common.{Flow, Energy}
import scala.concurrent.duration.FiniteDuration

/**
 * Complete input required to evolve one grid entity for a simulation tick.
 *
 * The request keeps the generic dispatcher independent of concrete entity
 * families while giving each handler access to the immutable model, its
 * current dynamic state, the environment, and the duration being simulated.
 *
 * @param entity immutable entity model to evolve
 * @param state current dynamic state associated with `entity`
 * @param env environment observed during the tick
 * @param delta amount of simulated time represented by the tick
 */
final case class EvolutionRequest(
  entity: GridEntity,
  state: GridEntityState,
  env: Environment,
  delta: FiniteDuration
)

/**
 * Pluggable evolution strategy for one supported entity-state family.
 *
 * Implementations encapsulate family-specific dependencies and type matching.
 * A dispatcher first calls [[supports]] and delegates to [[evolve]] only when
 * it returns `true`. Consequently, implementations may treat support as a
 * precondition of evolution and reject incompatible requests.
 *
 * New entity families can be introduced by implementing this contract and
 * registering the handler with an [[EntityEvolutionDispatcher]].
 */
trait EntityEvolutionHandler:

  /**
   * Determines whether this handler can evolve the supplied entity-state pair.
   *
   * This method should inspect request compatibility only and must not perform
   * the evolution or mutate external state.
   *
   * @param request candidate evolution request
   * @return `true` when this handler can safely process `request`
   */
  def supports(request: EvolutionRequest): Boolean

  /**
   * Evolves a request accepted by [[supports]].
   *
   * @param request supported entity-state pair and tick context
   * @return the updated state and residual grid flow
   * @throws IllegalArgumentException if the implementation receives an
   *         unsupported request
   */
  def evolve(request: EvolutionRequest): (GridEntityState, Flow[Energy])
