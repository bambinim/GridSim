package org.gridsim.core.model

import org.gridsim.core.common.*
import org.gridsim.core.model.storage.StorageState.percentage
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.validation.BatteryValidator.given
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BatterySpec extends AnyFlatSpec with Matchers {

  private val validBattery = Battery(
    id = "Battery 1",
    maxCapacity = 10.kwh,
    maxPowerCharge = 5.kw,
    maxPowerDischarge = 5.kw,
    minSoC = 0.2
  )

  "A Battery" should "be correctly initialized with its specs and state" in {
    val state = BatteryState(entityId = "Battery 1", currentCharge = 5.kwh)
    val result = Battery.make(validBattery, state)

    result.isValid shouldBe true
    val (entity, s) = result.getOrElse(fail())
    entity.id shouldBe "Battery 1"
    entity.maxCapacity.toDouble shouldBe 10.kwh.toDouble
    s shouldBe state
  }

  it should "calculate correctly its battery level" in {
    val state = BatteryState("Battery 1", 5.kwh)
    validBattery.percentage(state) shouldBe 0.5
  }

  it should "report 1.0 when full" in {
    val state = BatteryState("Battery 1", 10.kwh)
    validBattery.percentage(state) shouldBe 1.0
  }

  it should "report 0.0 when empty" in {
    val state = BatteryState("Battery 1", 0.kwh)
    validBattery.percentage(state) shouldBe 0.0
  }
}
