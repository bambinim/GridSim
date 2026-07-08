package org.gridsim.core.validation

import org.gridsim.core.common.*
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.error.DomainError.{InvalidId, OutOfRange, ValueMustBePositive}
import cats.syntax.all.*
import org.gridsim.core.behaviour.house.DefaultConsumptionStrategy
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HouseValidatorSpec extends AnyFlatSpec with Matchers {

  "HouseValidator" should "report an InvalidId error when the ID is too short" in {
    val entity = House(
      id = "ID",
      components = Nil,
      strategy = DefaultConsumptionStrategy.traditionalProfile
    )
    val state = HouseState(entityId = "ID", componentStates = Nil)
    val result = House.make(entity, state)

    result.isInvalid shouldBe true

    result.fold(
      errors => errors.toList should contain (DomainError.EntityError("ID", InvalidId("House Id", "ID"))),
      _ => fail("It should have failed")
    )
  }

  it should "accumulate multiple errors from both the house and its components" in {
    val battery = Battery(
      id = "Battery",
      maxCapacity = -10.kwh,
      maxPowerCharge = 5.kw,
      maxPowerDischarge = 5.kw,
      minSoC = 0.2
    )
    val bState = BatteryState(entityId = "Battery", currentCharge = 0.kwh)
    
    val entity = House(
      id = "X",
      components = List(battery),
      strategy = DefaultConsumptionStrategy.traditionalProfile
    )
    val state = HouseState(entityId = "X", componentStates = List(bState))
    
    val result = House.make(entity, state)

    result.fold(
      errors => {
        val errorsList = errors.toList
        errorsList.size shouldBe 3

        errorsList should contain (DomainError.EntityError("X", InvalidId("House Id", "X")))
        errorsList should contain (DomainError.EntityError("X", DomainError.EntityError("Battery", ValueMustBePositive("Capacity", -10.0))))
        errorsList should contain (DomainError.EntityError("X", DomainError.EntityError("Battery", OutOfRange("Current Charge", 0.0, 0.0, -10.0))))
      },
      _ => fail("It should have failed")
    )
  }

  it should "validate that all components are valid" in {
    val battery = Battery(
      id = "Battery",
      maxCapacity = 10.kwh,
      maxPowerCharge = -5.kw,
      maxPowerDischarge = 5.kw,
      minSoC = 0.2
    )
    val bState = BatteryState(entityId = "Battery", currentCharge = 0.kwh)

    val entity = House(
      id = "House 1",
      components = List(battery),
      strategy = DefaultConsumptionStrategy.traditionalProfile
    )
    val state = HouseState(entityId = "House 1", componentStates = List(bState))
    
    val result = House.make(entity, state)

    result.fold(
      errors => {
        val errorsList = errors.toList
        errorsList.size shouldBe 1

        errorsList should contain(DomainError.EntityError("House 1", DomainError.EntityError("Battery", ValueMustBePositive("Max Power Charge", -5.0))))
      },
      _ => fail("It should have failed")
    )
  }

}
