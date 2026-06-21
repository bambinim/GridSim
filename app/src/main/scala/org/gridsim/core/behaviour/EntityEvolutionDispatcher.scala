package org.gridsim.core.behaviour

import org.gridsim.core.behaviour.house.*
import org.gridsim.core.behaviour.house.HouseEvolution.evolve as evolveHouse
import org.gridsim.core.behaviour.producer.SolarPanelEvolution.evolve as evolveSolarPanel
import org.gridsim.core.behaviour.shaping.DemandShaper
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.house.{House, HouseState}
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

object EntityEvolutionDispatcher:
  given default(using
    resolver: ConsumptionResolver,
    shaper: DemandShaper
  ): EntityEvolutionDispatcher =
    DefaultEntityEvolutionDispatcher(
      HouseEvolutionDependencies(resolver, shaper)
    )

/**
 * Central dispatcher for the entity families currently supported by GridSim.
 */
final case class DefaultEntityEvolutionDispatcher(
  houseDependencies: HouseEvolutionDependencies
) extends EntityEvolutionDispatcher:

  override def evolve(
    state: GridEntityState,
    entity: GridEntity,
    environment: Environment,
    delta: FiniteDuration
  ): (GridEntityState, Flow[Energy]) =
    (state, entity) match
      case (houseState: HouseState, house: House) =>
        given EvolutionContext[HouseEvolutionDependencies] =
          EvolutionContext(delta, houseDependencies)

        houseState.evolveHouse(house, environment)

      case (panelState: SolarPanelState, panel: SolarPanel) =>
        given EvolutionContext[Unit] =
          EvolutionContext(delta, ())

        panelState.evolveSolarPanel(panel, environment)

      case _ =>
        throw IllegalArgumentException(
          s"Unsupported entity-state pair: model '${entity.id}', state '${state.entityId}'"
        )
