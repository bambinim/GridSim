package org.gridsim.core.behaviour

import org.gridsim.core.behaviour.producer.SolarPanelEvolution.evolve
import org.gridsim.core.common.*
import org.gridsim.core.model.{Environment, SolarPanel, SolarPanelState}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class SolarPanelEvolutionSpec extends AnyFlatSpec with Matchers:

  import org.gridsim.core.validation.SolarPanelValidator.given

  private val location = GeographicPoint(44.3, 11.7)

  private val state = SolarPanelState("panel-01")

  private val panel =
    SolarPanel(
      "panel-01",
      location,
      maxProduction = 5.0.kw,
      areaSqm = 20.0,
      efficiency = 0.20,
      state = state
    ).toOption.get.panel

  "SolarPanelEvolution" should "produce energy using the irradiance from the environment" in:
    val environment = Environment(6.hour)

    given EvolutionContext[Unit] = EvolutionContext(delta = 1.hour, Nil: Unit)

    state.currentProduction.toDouble shouldBe 0.0

    val (nextState, flow) = state.evolve(panel, environment)

    nextState.currentProduction.toDouble should be >= 0.0
    flow shouldBe Flow.Surplus(nextState.currentProduction)
