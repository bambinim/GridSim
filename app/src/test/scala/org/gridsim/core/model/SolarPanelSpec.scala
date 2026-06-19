package org.gridsim.core.model

import org.gridsim.core.common.{GeographicPoint, Power, kw}
import org.gridsim.core.validation.SolarPanelValidator.given
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SolarPanelSpec extends AnyFlatSpec with Matchers:

  private val location  = GeographicPoint(44.3, 11.7)
  private val validSpec = SolarPanelSpecification(5.0.kw, areaSqm = 20.0, efficiency = 0.20)
  private val zeroState = SolarPanelState(Power.Zero)

  // Construction via smart constructor

  "SolarPanel" should "be valid when all fields are within range" in:
    SolarPanel("panel-01", zeroState, location, validSpec) shouldBe a[cats.data.Validated.Valid[?]]

  it should "default to Standard physics when physics is omitted" in:
    val result = SolarPanel("panel-01", zeroState, location, validSpec)
    result.map(_.physics) shouldBe cats.data.Validated.valid(SolarPanelPhysics.Standard)

  it should "default to zero output power when state is omitted" in:
    val result = SolarPanel("panel-01", location = location, spec = validSpec)
    result.map(_.state.outputPower) shouldBe cats.data.Validated.valid(Power.Zero)

  it should "be invalid when peak power is zero" in:
    val result = SolarPanel("panel-01", zeroState, location,
      validSpec.copy(peakPower = 0.0.kw))
    result.isInvalid shouldBe true

  it should "be invalid when area is zero or negative" in:
    SolarPanel("panel-01", zeroState, location, validSpec.copy(areaSqm = 0.0)).isInvalid shouldBe true
    SolarPanel("panel-01", zeroState, location, validSpec.copy(areaSqm = -1.0)).isInvalid shouldBe true

  it should "be invalid when efficiency is out of (0, 1] range" in:
    SolarPanel("panel-01", zeroState, location, validSpec.copy(efficiency = -0.1)).isInvalid shouldBe true
    SolarPanel("panel-01", zeroState, location, validSpec.copy(efficiency = 1.01)).isInvalid shouldBe true

  it should "accumulate multiple errors when several fields are invalid" in:
    val result = SolarPanel("panel-01", zeroState, location,
      SolarPanelSpecification(-0.1.kw, areaSqm = -5.0, efficiency = 1.0))
    result match
      case cats.data.Validated.Invalid(errors) => errors.length shouldBe 3
      case _                                   => fail("Expected invalid")

  // Field exposure

  it should "expose location, specification and physics after successful construction" in:
    val panel = SolarPanel("panel-01", zeroState, location, validSpec).toOption.get
    panel.location      shouldBe location
    panel.specification shouldBe validSpec
    panel.physics       shouldBe SolarPanelPhysics.Standard

  // withState

  it should "return a new panel with updated state via withState" in:
    val panel = SolarPanel("panel-01", zeroState, location, validSpec).toOption.get
    val newState = SolarPanelState(3.0.kw)
    val updated = panel.withState(newState)
    updated.state.outputPower.toDouble shouldBe 3.0

  it should "not mutate the original panel when withState is called" in:
    val panel = SolarPanel("panel-01", zeroState, location, validSpec).toOption.get
    panel.withState(SolarPanelState(3.0.kw))
    panel.state.outputPower shouldBe Power.Zero
