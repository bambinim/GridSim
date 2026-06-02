package org.gridsim.core.model

import org.gridsim.core.common.Units.*
import org.gridsim.core.model.battery.{Battery, BatterySpecification, BatteryState}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BatterySpec extends AnyFlatSpec with Matchers {

  "A Battery" should "be correctly initialized with its specs and state" in {
    val spec = BatterySpecification(
      capacity = 10.0.kwh,
      maxPowerCharge = 3.0.kw,
      maxPowerDischarge = 3.0.kw,
      minSoC = 0.1
    )
    val state = BatteryState(currentCharge = 5.0.kwh)
    val result = Battery.makeBattery(spec, state)

    result.isValid shouldBe true
  }

  it should "be invalid if specifications are invalid" in {
    val spec = BatterySpecification(
      capacity = 10.0.kwh,
      maxPowerCharge = -3.0.kw,
      maxPowerDischarge = 3.0.kw,
      minSoC = 1.2
    )

    val state = BatteryState(currentCharge = 5.kwh)
    val result = Battery.makeBattery(spec, state)
    result.isInvalid shouldBe true
  }

  it should "be invalid if state are invalid" in {
    val spec = BatterySpecification(
      capacity = 10.0.kwh,
      maxPowerCharge = 3.0.kw,
      maxPowerDischarge = 3.0.kw,
      minSoC = 0.1
    )

    val state = BatteryState(currentCharge = 20.kwh)
    val result = Battery.makeBattery(spec, state)
    result.isInvalid shouldBe true
  }

}
