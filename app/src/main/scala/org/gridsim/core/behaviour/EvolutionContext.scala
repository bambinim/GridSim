package org.gridsim.core.behaviour

import scala.concurrent.duration.FiniteDuration

/**
 * Dependencies shared by an entity evolution for one simulation tick.
 *
 * @param delta the duration represented by one simulation tick
 * @param dependencies services and configuration required by a specific entity evolution
 * @tparam D the entity-specific dependency type
 */
final case class EvolutionContext[D](
  delta: FiniteDuration,
  dependencies: D
)

object EvolutionContext:

  /** Builds a context from the tick duration and an entity-specific dependency value. */
  given fromGivens[D](using
    delta: FiniteDuration,
    dependencies: D
  ): EvolutionContext[D] =
    EvolutionContext(delta, dependencies)
