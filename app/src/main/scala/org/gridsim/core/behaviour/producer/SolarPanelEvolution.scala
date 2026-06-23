package org.gridsim.core.behaviour.producer

import org.gridsim.core.behaviour.{EvolutionContext, GridEvolution}
import org.gridsim.core.common.*
import org.gridsim.core.model.{Environment, SolarPanel, SolarPanelState}
import org.gridsim.core.behaviour.producer.SolarPanelStrategy.produce

import scala.concurrent.duration.FiniteDuration

object SolarPanelEvolution extends GridEvolution[SolarPanelState, SolarPanel, EvolutionContext[Unit]]:
  extension (state: SolarPanelState)
    def evolve(panel: SolarPanel, environment: Environment)(using context: EvolutionContext[Unit]): (SolarPanelState, Flow[Energy]) =
      given FiniteDuration = context.delta
      given SolarPanelStrategy = SolarPanelStrategy.forPhysics(panel.physics)

      val weather = environment.weather(panel.location)
      val (nextState, produced) = state.produce(panel, weather.irradiance)

      (nextState, produced)
