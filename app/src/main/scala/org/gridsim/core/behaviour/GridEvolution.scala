package org.gridsim.core.behaviour

import org.gridsim.core.common.*
import org.gridsim.core.model.*

/**
 * Defines how a grid entity and its state advance by one simulation tick.
 *
 * The trait intentionally exposes only an extension method so callers can write
 * `state.evolve(entity, env)`. Implementations choose their own context type `C`
 * for dependencies that are specific to that entity family.
 */
trait GridEvolution[S <: GridEntityState, E <: GridEntity, C]:
  extension (state: S)
    /**
     * Evolves `state` for `entity` within the supplied environment.
     *
     * @return the updated state and the residual energy flow left after the entity has acted.
     */
    def evolve(entity: E, env: Environment)(using context: C): (S, Flow[Energy])
