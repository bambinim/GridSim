package org.gridsim.core.validation

import org.gridsim.core.common.*
import org.gridsim.core.model.battery.{Battery, BatteryModel, BatteryState}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.error.DomainError.{OutOfRange, ValueMustBePositive}
import org.gridsim.core.validation.BatteryValidator.given
import cats.syntax.all.*
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BatteryValidatorSpec extends AnyFlatSpec with Matchers {

  private val validBattery = Battery(
    id = "bat-1",
    maxCapacity = 100.kwh,
    maxPowerCharge = 5.kw,
    maxPowerDischarge = 5.kw
  )
  private val validState = BatteryState(entityId = "bat-1", currentCharge = 50.kwh)

  "BatteryValidator" should "accept a valid battery" in {
    val result = Battery.make(validBattery, validState)
    result.isValid shouldBe true
  }

  it should "reject negative capacity" in {
    val invalidBattery = validBattery.copy(maxCapacity = (-10).kwh)
    val result = Battery.make(invalidBattery, validState)

    result.fold(
      errors => errors.toList should contain(ValueMustBePositive("Capacity", -10.0)),
      _ => fail("It should have failed")
    )
  }

  it should "reject zero or negative power rates" in {
    val invalidBattery = validBattery.copy(maxPowerCharge = 0.kw, maxPowerDischarge = -5.kw)
    val result = Battery.make(invalidBattery, validState)

    result.fold(
      errors => {
        val errorsList = errors.toList
        errorsList.size shouldBe 2

        errorsList should contain(ValueMustBePositive("Max Power Charge", 0.0))
        errorsList should contain(ValueMustBePositive("Max Power Discharge", -5.0))
      },
      _ => fail("It should have failed")
    )
  }

  it should "reject out of range minSoC" in {
    val invalidBattery = validBattery.copy(minSoC = 1.1)
    val result = Battery.make(invalidBattery, validState)

    result.fold(
      errors => errors.toList should contain(OutOfRange("Min SoC", 1.1, 0.0, 1.0)),
      _ => fail("It should have failed")
    )
  }

  it should "reject charge exceeding capacity" in {
    val invalidState = validState.copy(currentCharge = 150.kwh)
    val result = Battery.make(validBattery, invalidState)

    result.fold(
      errors => errors.toList should contain(OutOfRange("Current Charge", 150.0, 0.0, 100.0)),
      _ => fail("It should have failed")
    )
  }

  it should "reject negative charge" in {
    val invalidState = validState.copy(currentCharge = (-1).kwh)
    val result = Battery.make(validBattery, invalidState)

    result.fold(
      errors => errors.toList should contain(OutOfRange("Current Charge", -1.0, 0.0, 100.0)),
      _ => fail("It should have failed")
    )
  }
}
