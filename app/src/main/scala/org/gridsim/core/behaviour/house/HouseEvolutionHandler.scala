package org.gridsim.core.behaviour.house

import org.gridsim.core.behaviour.{EntityEvolutionHandler, EvolutionContext, EvolutionRequest}
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.behaviour.house.HouseEvolution.evolve as evolveHouse
import org.gridsim.core.common.{Flow, Energy}

/**
 * Adapts the typed [[HouseEvolution]] program to the generic entity evolution
 * dispatch contract.
 *
 * This handler accepts only requests pairing a [[HouseState]] with a [[House]].
 * It builds the per-tick [[EvolutionContext]] from the request duration and the
 * injected house services before delegating the actual domain transition to
 * [[HouseEvolution]].
 *
 * @param dependencies services and strategies required to evolve a house
 */
final case class HouseEvolutionHandler(
  dependencies: HouseEvolutionDependencies
) extends EntityEvolutionHandler:

  /**
   * Tests whether the request contains a compatible house state and model.
   *
   * @param request candidate request
   * @return `true` for a `HouseState`/`House` pair, otherwise `false`
   */
  override def supports(request: EvolutionRequest): Boolean =
    (request.state, request.entity) match
      case (_: HouseState, _: House) => true
      case _                         => false

  /**
   * Evolves a supported house for one tick.
   *
   * @param request house model, state, environment, and tick duration
   * @return the updated house state and its residual grid flow
   * @throws IllegalArgumentException if the request is not a
   *         `HouseState`/`House` pair
   */
  override def evolve(request: EvolutionRequest): (HouseState, Flow[Energy]) =
    (request.state, request.entity) match
      case (s: HouseState, h: House) =>
        given EvolutionContext[HouseEvolutionDependencies] =
          EvolutionContext(request.delta, dependencies)

        val (nextState, flow) = s.evolveHouse(h, request.env)
        (nextState, flow)

      case _ =>
        throw IllegalArgumentException(
          s"${request.entity.id} handler received an unsupported request"
        )
