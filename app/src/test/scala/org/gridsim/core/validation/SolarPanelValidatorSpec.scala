package org.gridsim.core.validation

import cats.data.Validated
import org.gridsim.core.common.{GeographicPoint, Power, kw}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.{SolarPanel, SolarPanelPhysics, SolarPanelState}
import org.gridsim.core.validation.SolarPanelValidator.given
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SolarPanelValidatorSpec extends AnyFlatSpec with Matchers:

  private val location = GeographicPoint(44.3, 11.7)

  private def validate(
                        id: String = "panel-01",
                        maxProduction: Power = 5.0.kw,
                        areaSqm: Double = 20.0,
                        efficiency: Double = 0.20,
                        state: Option[SolarPanelState] = None,
                      ) = SolarPanel(id, location, maxProduction, areaSqm, efficiency, state)

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
    validate(areaSqm = 0.0).isInvalid shouldBe true

  it should "reject negative area" in:
    validate(areaSqm = -10.0).isInvalid shouldBe true

  it should "reject efficiency exactly at 0.0" in:
    validate(efficiency = 0.0).isInvalid shouldBe true

  it should "accept efficiency exactly at 1.0 (boundary)" in:
    validate(efficiency = 1.0) shouldBe a[Validated.Valid[?]]

  it should "reject efficiency above 1.0" in:
    validate(efficiency = 1.01).isInvalid shouldBe true

  // State validation

  it should "accept output power of zero (idle panel)" in:
    validate(state = Some(SolarPanelState("panel-01", 0.0))) shouldBe a[Validated.Valid[?]]

  it should "accept output power equal to peak power (full production)" in:
    validate(state = Some(SolarPanelState("panel-01", 0.20))) shouldBe a[Validated.Valid[?]]

  it should "reject output power above peak power" in:
    val result = validate(state = Some(SolarPanelState("panel-01", 0.21)))
    result.isInvalid shouldBe true
    result.swap.toOption.get.exists(_.isInstanceOf[DomainError.OutOfRange]) shouldBe true

  it should "reject negative output power" in:
    validate(state = Some(SolarPanelState("panel-01", -6.0))).isInvalid shouldBe true

  // Error accumulation

  it should "accumulate all three spec errors independently" in:
    val result = validate(maxProduction = 0.0.kw, areaSqm = -5.0, efficiency = 2.0)
    result match
      case Validated.Invalid(errors) => errors.length shouldBe 3
      case _                         => fail("Expected all three spec errors")

  it should "accumulate spec and state errors together" in:
    val result = validate(maxProduction = 0.0.kw, areaSqm = -5.0, efficiency = 0.0, state = Some(SolarPanelState("panel-01", -1.0)))
    result match
      case Validated.Invalid(errors) => errors.length shouldBe 4
      case _                         => fail("Expected combined spec + state errors")
