package org.gridsim.core.behaviour.producer

import org.gridsim.core.behaviour.{EntityEvolutionHandler, EvolutionContext, EvolutionRequest}
import org.gridsim.core.model.{SolarPanelState, SolarPanel}
import org.gridsim.core.behaviour.producer.SolarPanelEvolution.evolve as evolveSolarPanel
import org.gridsim.core.common.{Flow, Energy}

/**
 * Adapts standalone solar-panel evolution to the generic entity dispatcher.
 *
 * The handler accepts only a [[SolarPanelState]] paired with a [[SolarPanel]].
 * Solar evolution has no external service dependencies, so its per-tick
 * [[EvolutionContext]] carries `Unit` together with the request duration.
 */
final case class SolarPanelEvolutionHandler() extends EntityEvolutionHandler:

  /**
   * Tests whether the request contains a compatible solar-panel state and
   * model.
   *
   * @param request candidate request
   * @return `true` for a `SolarPanelState`/`SolarPanel` pair, otherwise `false`
   */
  override def supports(request: EvolutionRequest): Boolean =
    (request.state, request.entity) match
      case (_: SolarPanelState, _: SolarPanel) => true
      case _                         => false

  /**
   * Evolves a supported standalone solar panel for one tick.
   *
   * @param request panel model, state, environment, and tick duration
   * @return the updated panel state and its produced energy flow
   * @throws IllegalArgumentException if the request is not a
   *         `SolarPanelState`/`SolarPanel` pair
   */
  override def evolve(request: EvolutionRequest): (SolarPanelState, Flow[Energy]) =
    (request.state, request.entity) match
      case (s: SolarPanelState, p: SolarPanel) =>
        given EvolutionContext[Unit] =
          EvolutionContext(request.delta, ())

        val (nextState, flow) = s.evolveSolarPanel(p, request.env)
        (nextState, flow)

      case _ =>
        throw IllegalArgumentException(
          s"${request.entity.id} handler received an unsupported request"
        )
