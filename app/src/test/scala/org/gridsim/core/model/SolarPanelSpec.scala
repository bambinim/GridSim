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

  private def validate(
                    id: String = "panel-01",
                    maxProduction: Power = 5.0.kw,
                    areaSqm: Double = 20.0,
                    efficiency: Double = 0.20
                  ) = SolarPanel(id, location, maxProduction, areaSqm, efficiency)

  // Construction

  "SolarPanel" should "be valid when all fields are within range" in:
    validate() shouldBe a[cats.data.Validated.Valid[?]]

  it should "default to Standard physics when physics is omitted" in:
    val (panel, _) = validate().toOption.get
    panel.physics shouldBe SolarPanelPhysics.Standard

  it should "default state efficiency to panel efficiency" in:
    val (panel, state) = validate().toOption.get
    state.efficiency shouldBe panel.efficiency

  it should "be invalid when peak power is zero" in:
    validate(maxProduction = 0.0.kw).isInvalid shouldBe true

  it should "be invalid when area is zero or negative" in:
    validate(areaSqm = 0.0).isInvalid shouldBe true
    validate(areaSqm = -1.0).isInvalid shouldBe true

  it should "be invalid when efficiency is out of (0, 1] range" in:
    validate(efficiency = -0.1).isInvalid shouldBe true
    validate(efficiency = 1.01).isInvalid shouldBe true

  it should "accumulate multiple errors when several fields are invalid" in:
    val result = validate(maxProduction = -0.1.kw, areaSqm = -5.0, efficiency = 0.0)
    result match
      case cats.data.Validated.Invalid(errors) => errors.length shouldBe 3
      case _                                   => fail("Expected invalid")

  // Field exposure

  it should "expose location and physics after successful construction" in:
    val (panel, _) = validate().toOption.get
    panel.location shouldBe location
    panel.physics  shouldBe SolarPanelPhysics.Standard
