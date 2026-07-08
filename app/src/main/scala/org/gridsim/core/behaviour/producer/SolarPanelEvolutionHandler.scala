package org.gridsim.core.behaviour.producer

import org.gridsim.core.behaviour.{EntityEvolutionHandler, EvolutionContext}
import org.gridsim.core.behaviour.producer.SolarPanelEvolution.evolve as evolveSolarPanel
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.{Environment, GridEntity, GridEntityState, SolarPanel, SolarPanelState}

import scala.concurrent.duration.FiniteDuration

/**
 * Adapts [[SolarPanelEvolution]] to the generic entity evolution dispatcher.
 */
object SolarPanelEvolutionHandler extends EntityEvolutionHandler:

  override val stateClass: Class[SolarPanelState] = classOf[SolarPanelState]
  override val entityClass: Class[SolarPanel] = classOf[SolarPanel]

  /**
   * Evolves a solar panel state-model pair by one simulation tick.
   */
  override def evolve(
    state: GridEntityState,
    entity: GridEntity,
    environment: Environment,
    delta: FiniteDuration
  ): (GridEntityState, Flow[Energy]) =
    given EvolutionContext[Unit] =
      EvolutionContext(delta, ())

    stateClass
      .cast(state)
      .evolveSolarPanel(entityClass.cast(entity), environment)
