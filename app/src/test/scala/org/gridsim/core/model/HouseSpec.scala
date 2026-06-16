package org.gridsim.core.model

import org.gridsim.core.common.*
import org.gridsim.core.model.house.Occupancy.Traditional
import org.gridsim.core.model.house.{House, Size}
import org.gridsim.core.model.battery.{Battery, BatterySpecification, BatteryState}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HouseSpec extends AnyFlatSpec with Matchers {

  "A House" should "be correctly initialized with valid structural data" in {
    val result = House.makeEmptyHouse("ValidHouse123")

    result.isValid shouldBe true
    val house = result.getOrElse(fail("Should be valid"))
    house.id shouldBe "ValidHouse123"
    house.producers shouldBe empty
    house.storages shouldBe empty
  }

  it should "be valid when initialized with storages" in {
    val spec = BatterySpecification(10.kwh, 5.kw, 5.kw, 0.2)
    val battery = Battery("Battery1", spec, BatteryState(5.kwh))

    val result = House.makeHouseWithStorages("HouseWithBattery", List(battery))

    result.isValid shouldBe true
    val house = result.getOrElse(fail("Should be valid"))
    house.storages should have size 1
    house.storages.head.id shouldBe "Battery1"
  }

  "House Validation" should "fail if the ID is too short" in {
    val result = House.makeEmptyHouse("H1")

    result.isInvalid shouldBe true
  }

  it should "fail if any of its components are invalid" in {
    val invalidSpec = BatterySpecification(-10.kwh, 5.kw, 5.kw, 0.2)
    val invalidBattery = Battery("Inv", invalidSpec, BatteryState(0.kwh))

    val result = House.makeHouseWithStorages("HouseWithInvalidComp", List(invalidBattery))

    result.isInvalid shouldBe true
  }

  it should "accumulate errors from multiple invalid components" in {
    val result = House.makeEmptyHouse("H")

    result.isInvalid shouldBe true
  }
}