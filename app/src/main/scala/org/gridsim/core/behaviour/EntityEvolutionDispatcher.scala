package org.gridsim.core.behaviour

import org.gridsim.core.behaviour.house.{HouseEvolutionDependencies, HouseEvolutionHandler}
import org.gridsim.core.behaviour.house.HouseEvolution.evolve as evolveHouse
import org.gridsim.core.behaviour.producer.SolarPanelEvolution.evolve as evolveSolarPanel
import org.gridsim.core.behaviour.producer.SolarPanelEvolutionHandler
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.*
import org.gridsim.core.model.house.{House, HouseState}

import scala.concurrent.duration.FiniteDuration

/**
 * Dispatches a generic entity-state pair to its concrete evolution.
 *
 * The simulation engine depends only on this algebra and does not need to know
 * the concrete entity families supported by the application.
 */
trait EntityEvolutionDispatcher:
  /**
   * Evolves the entity-state pair contained in `request` by one simulation
   * tick.
   *
   * @param request entity model, current state, environment, and tick duration
   * @return the updated entity state and its residual energy flow
   * @throws IllegalArgumentException if no registered evolution supports the
   *         supplied entity-state pair
   */
  def evolve(
    request: EvolutionRequest
  ): (GridEntityState, Flow[Energy])

/**
 * Registry-backed [[EntityEvolutionDispatcher]] that delegates a request to
 * its matching [[EntityEvolutionHandler]].
 *
 * Handler selection is based on [[EntityEvolutionHandler.supports]]. A valid
 * registry must contain exactly one matching handler for every entity-state
 * pair that can occur in the simulation. Concrete evolution dependencies stay
 * encapsulated in their handlers, allowing new entity families to be added
 * without changing this dispatcher.
 *
 * @param handlers available entity evolution implementations
 */
final case class DefaultEntityEvolutionDispatcher(
  handlers: Iterable[EntityEvolutionHandler]
) extends EntityEvolutionDispatcher:

  /**
   * Selects the handler supporting `request` and delegates its evolution.
   *
   * @param request entity-state pair and tick context to evolve
   * @return the state and residual flow returned by the selected handler
   * @throws IllegalArgumentException if no handler supports `request`
   */
  override def evolve(
    request: EvolutionRequest
  ): (GridEntityState, Flow[Energy]) =
    handlers.filter(_.supports(request)) match
      case handler :: Nil =>
        val (newState, flow) = handler.evolve(request)
        newState -> flow
      case Nil =>
        throw IllegalArgumentException(
          s"No evolution handler supports state " +
            s"'${request.state.getClass.getSimpleName}' and entity " +
            s"'${request.entity.getClass.getSimpleName}'"
        )

/** Factory and contextual instance for the application dispatcher. */
object EntityEvolutionDispatcher:

  /**
   * Builds the default dispatcher from the dependencies required by supported
   * entity evolutions. The resulting registry supports houses and standalone
   * solar panels.
   *
   * @param houseDependencies services required by house evolution
   * @return the standard application dispatcher
   */
  def default(using
    houseDependencies: HouseEvolutionDependencies
  ): EntityEvolutionDispatcher =
    DefaultEntityEvolutionDispatcher(List(
        HouseEvolutionHandler(houseDependencies),
        SolarPanelEvolutionHandler()
      )
    )

  /**
   * Supplies the default dispatcher for simulation components that request one
   * contextually.
   *
   * @param houseDependencies services required to construct the house handler
   * @return the standard application dispatcher
   */
  given defaultDispatcher(using
    houseDependencies: HouseEvolutionDependencies
  ): EntityEvolutionDispatcher =
    default
