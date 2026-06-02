package org.gridsim.core.model

import org.gridsim.core.common.Units.*
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
    val battery = Battery(spec, state)

    battery.spec.capacity shouldBe 10.0.kwh
    battery.state.currentCharge shouldBe 5.0.kwh
    battery.spec.minSoC shouldBe 0.1
  }

}