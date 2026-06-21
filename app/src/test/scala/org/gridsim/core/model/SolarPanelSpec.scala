package org.gridsim.core.model

import org.gridsim.core.common.{GeographicPoint, Power, kw}
import org.gridsim.core.validation.SolarPanelValidator.given
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SolarPanelSpec extends AnyFlatSpec with Matchers:

  private val location   = GeographicPoint(44.3, 11.7)
  private val peakPower  = 5.0.kw
  private val areaSqm    = 20.0
  private val efficiency = 0.20
  private val zeroState  = SolarPanelState("panel-01")

  private def make(
                    id: String = "panel-01",
                    maxProduction: Power = peakPower,
                    area: Double = areaSqm,
                    eff: Double = efficiency,
                    state: SolarPanelState = zeroState,
                    physics: SolarPanelPhysics = SolarPanelPhysics.Standard
                  ) = SolarPanel(id, location, maxProduction, area, eff, state, physics)

  // Construction

  "SolarPanel" should "be valid when all fields are within range" in:
    make() shouldBe a[cats.data.Validated.Valid[?]]

  it should "default to Standard physics when physics is omitted" in:
    make().map(_.panel.physics) shouldBe cats.data.Validated.valid(SolarPanelPhysics.Standard)

  it should "default to zero output power when state is omitted" in:
    make().map(_.state.currentProduction) shouldBe cats.data.Validated.valid(Power.Zero)

  it should "be invalid when peak power is zero" in:
    make(maxProduction = 0.0.kw).isInvalid shouldBe true

  it should "be invalid when area is zero or negative" in:
    make(area = 0.0).isInvalid shouldBe true
    make(area = -1.0).isInvalid shouldBe true

  it should "be invalid when efficiency is out of (0, 1] range" in:
    make(eff = -0.1).isInvalid shouldBe true
    make(eff = 1.01).isInvalid shouldBe true

  it should "accumulate multiple errors when several fields are invalid" in:
    val result = make(maxProduction = -0.1.kw, area = -5.0, eff = 1.0)
    result match
      case cats.data.Validated.Invalid(errors) => errors.length shouldBe 3
      case _                                   => fail("Expected invalid")

  // Field exposure

  it should "expose location and physics after successful construction" in:
    val panelWithState = make().toOption.get
    panelWithState.panel.location shouldBe location
    panelWithState.panel.physics  shouldBe SolarPanelPhysics.Standard

  // withState (via copy on SolarPanelWithState)

  it should "return a new SolarPanelWithState with updated state" in:
    val panelWithState = make().toOption.get
    val newState = SolarPanelState("panel-01", 3.0.kw)
    val updated  = panelWithState.copy(state = newState)
    updated.state.currentProduction.toDouble shouldBe 3.0

  it should "not mutate the original SolarPanelWithState when copy is called" in:
    val panelWithState = make().toOption.get
    panelWithState.copy(state = SolarPanelState("panel-01", 3.0.kw))
    panelWithState.state.currentProduction shouldBe Power.Zero
