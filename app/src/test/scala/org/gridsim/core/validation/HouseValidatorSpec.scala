package org.gridsim.core.validation

import org.gridsim.core.model.house.Occupancy.Traditional
import org.gridsim.core.model.house.{House, Size}
import org.gridsim.core.model.battery.{Battery, BatterySpecification, BatteryState}
import org.gridsim.core.model.error.DomainError.{InvalidId, OutOfRange, ValueMustBePositive}
import org.gridsim.core.model.house.Size.{Medium, Small}
import org.gridsim.core.common.Units.{kw, kwh}
import cats.syntax.all.*
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HouseValidatorSpec extends AnyFlatSpec with Matchers {

  "HouseValidator" should "report an InvalidId error when the ID is too short" in {
    val result = House.makeEmptyHouse("ID", Medium, Traditional)

    result.isInvalid shouldBe true

    result.fold(
      errors => errors.toList should contain (InvalidId("House Id", "ID")),
      _ => fail("It should have be failed")
    )
  }

  it should "accumulates multiple errors from both the house and its components" in {
    val invalidSpec = BatterySpecification(-10.kwh, 5.kw, 5.kw, 0.2)
    val battery = Battery("Battery", invalidSpec, BatteryState(0.kwh))

    val result = House.makeHouseWithStorages("X", Small, Traditional, List(battery))

    result.fold(
      errors => {
        val errorsList = errors.toList
        errorsList.size shouldBe 3

        errorsList should contain (InvalidId("House Id", "X"))
        errorsList should contain (ValueMustBePositive("Capacity", -10))
        errorsList should contain (OutOfRange("Current Charge", 0, 0, -10))
      },
      _ => fail("It should have be failed")
    )
  }

  it should "validate that all components are valid" in {
    val invalidSpec = BatterySpecification(10.kwh, -5.kw, 5.kw, 0.2)
    val battery = Battery("Battery", invalidSpec, BatteryState(0.kwh))

    val result = House.makeHouseWithStorages("House 1", Small, Traditional, List(battery))

    result.fold(
      errors => {
        val errorsList = errors.toList
        errorsList.size shouldBe 1

        errorsList should contain(ValueMustBePositive("Max Power Charge", -5))
      },
      _ => fail("It should have be failed")
    )
  }

}
