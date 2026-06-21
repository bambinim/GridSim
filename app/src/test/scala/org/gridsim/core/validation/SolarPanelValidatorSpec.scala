package org.gridsim.core.validation

import cats.data.Validated
import org.gridsim.core.common.{GeographicPoint, Power, kw}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.{SolarPanel, SolarPanelPhysics, SolarPanelState, SolarPanelWithState}
import org.gridsim.core.validation.SolarPanelValidator.given
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SolarPanelValidatorSpec extends AnyFlatSpec with Matchers:

  private val location   = GeographicPoint(44.3, 11.7)
  private val peakPower  = 5.0.kw
  private val areaSqm    = 20.0
  private val efficiency = 0.20
  private val zeroState  = SolarPanelState("panel-01")

  // Helper: construct and validate in one shot (mirrors the smart constructor path)
  private def validate(
                        maxProduction: Power   = peakPower,
                        area: Double           = areaSqm,
                        eff: Double            = efficiency,
                        state: SolarPanelState = zeroState
                      ) = SolarPanel("panel-01", location, maxProduction, area, eff, state)

  // Specification validation

  "SolarPanelValidator" should "accept a fully valid panel" in:
    validate() shouldBe a[Validated.Valid[?]]

  it should "reject peak power of zero" in:
    val result = validate(maxProduction = 0.0.kw)
    result.isInvalid shouldBe true
    result.swap.toOption.get.exists(_.isInstanceOf[DomainError.ValueMustBePositive]) shouldBe true

  it should "reject negative peak power" in:
    validate(maxProduction = -1.0.kw).isInvalid shouldBe true

  it should "reject area of zero" in:
    validate(area = 0.0).isInvalid shouldBe true

  it should "reject negative area" in:
    validate(area = -10.0).isInvalid shouldBe true

  it should "reject efficiency exactly at 0.0" in:
    validate(eff = 0.0).isInvalid shouldBe true

  it should "accept efficiency exactly at 1.0 (boundary)" in:
    validate(eff = 1.0) shouldBe a[Validated.Valid[?]]

  it should "reject efficiency above 1.0" in:
    validate(eff = 1.01).isInvalid shouldBe true

  // State validation

  it should "accept output power of zero (idle panel)" in:
    validate(state = SolarPanelState("panel-01", Power.Zero)) shouldBe a[Validated.Valid[?]]

  it should "accept output power equal to peak power (full production)" in:
    validate(state = SolarPanelState("panel-01", 5.0.kw)) shouldBe a[Validated.Valid[?]]

  it should "reject output power above peak power" in:
    val result = validate(state = SolarPanelState("panel-01", 5.001.kw))
    result.isInvalid shouldBe true
    result.swap.toOption.get.exists(_.isInstanceOf[DomainError.OutOfRange]) shouldBe true

  it should "reject negative output power" in:
    validate(state = SolarPanelState("panel-01", -1.0.kw)).isInvalid shouldBe true

  // Error accumulation

  it should "accumulate all three spec errors independently" in:
    val result = validate(maxProduction = 0.0.kw, area = -5.0, eff = 2.0)
    result match
      case Validated.Invalid(errors) => errors.length shouldBe 3
      case _                         => fail("Expected all three spec errors")

  it should "accumulate spec and state errors together" in:
    // peakPower=0 is invalid; state output=1.0 > 0 also triggers OutOfRange
    val result = validate(maxProduction = 0.0.kw, area = -5.0, eff = 2.0, state = SolarPanelState("panel-01", 1.0.kw))
    result match
      case Validated.Invalid(errors) => errors.length shouldBe 4
      case _                         => fail("Expected combined spec + state errors")
