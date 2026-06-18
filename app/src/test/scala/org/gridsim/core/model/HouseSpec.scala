package org.gridsim.core.model

import org.gridsim.core.common.*
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.behaviour.house.DefaultConsumptionStrategy
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.validation.HouseComponentValidator.given
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HouseSpec extends AnyFlatSpec with Matchers {
  
  "A House" should "be correctly initialized with valid structural data" in {
    val entity = House(
      id = "ValidHouse123",
      components = Nil,
      strategy = DefaultConsumptionStrategy.traditionalProfile
    )
    val state = HouseState(entityId = "ValidHouse123", componentStates = Nil)
    val result = House.make(entity, state)

    result.isValid shouldBe true
    val (e, s) = result.getOrElse(fail("Should be valid"))
    e.id shouldBe "ValidHouse123"
    e.components shouldBe empty
  }

  it should "be valid when initialized with storages" in {
    val battery = Battery(
      id = "Battery1",
      maxCapacity = 10.kwh,
      maxPowerCharge = 5.kw,
      maxPowerDischarge = 5.kw,
      minSoC = 0.2
    )
    val bState = BatteryState("Battery1", 5.kwh)
    
    val entity = House(
      id = "HouseWithBattery",
      components = List(battery),
      strategy = DefaultConsumptionStrategy.traditionalProfile
    )
    val state = HouseState("HouseWithBattery", List(bState))
    
    val result = House.make(entity, state)

    result.isValid shouldBe true
    val (e, s) = result.getOrElse(fail("Should be valid"))
    e.components should have size 1
    e.components.head.id shouldBe "Battery1"
  }

  "House Validation" should "fail if the ID is too short" in {
    val entity = House("H1", Nil)
    val state = HouseState("H1", Nil)
    val result = House.make(entity, state)
    
    result.isInvalid shouldBe true
  }

  it should "fail if any of its components are invalid" in {
    val invalidBattery = Battery(
      id = "Inv",
      maxCapacity = -10.kwh,
      maxPowerCharge = 5.kw,
      maxPowerDischarge = 5.kw,
      minSoC = 0.2
    )
    val bState = BatteryState("Inv", 0.kwh)

    val entity = House(
      id = "HouseWithInvalidComp",
      components = List(invalidBattery),
      strategy = DefaultConsumptionStrategy.traditionalProfile
    )
    val state = HouseState("HouseWithInvalidComp", List(bState))
    
    val result = House.make(entity, state)

    result.isInvalid shouldBe true
  }
}
