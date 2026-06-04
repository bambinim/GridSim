package org.gridsim.core.model

import org.gridsim.core.behaviour.BatteryBehaviour
import org.gridsim.core.common.Units.*
import org.gridsim.core.model.battery.*
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class BatterySpec extends AnyFlatSpec with Matchers {

  val spec = BatterySpecification(
    capacity = 10.0.kwh,
    maxPowerCharge = 5.0.kw,
    maxPowerDischarge = 5.0.kw,
    minSoC = 0.2
  )

  "A Battery" should "be correctly initialized with its specs and state" in {
    val state = BatteryState(currentCharge = 5.0.kwh)
    val result = Battery.makeBattery(spec, state)

    result.isValid shouldBe true
  }

  it should "be invalid if specifications are invalid" in {
    val specEr = BatterySpecification(
      capacity = 10.0.kwh,
      maxPowerCharge = -3.0.kw,
      maxPowerDischarge = 3.0.kw,
      minSoC = 1.2
    )

    val state = BatteryState(currentCharge = 5.kwh)
    val result = Battery.makeBattery(specEr, state)
    result.isInvalid shouldBe true
  }

  it should "be invalid if state is invalid" in {
    val state = BatteryState(currentCharge = 20.kwh)
    val result = Battery.makeBattery(spec, state)
    result.isInvalid shouldBe true
  }

  it should "calculate correctly its battery level" in {
    val battery = Battery(spec, BatteryState(5.kwh))
    battery.getBatteryLevel shouldBe 0.5
  }

  "BatteryBehaviour" should "charge correctly when within limits" in {
    val battery = Battery(spec, BatteryState(5.0.kwh))
    val (newBattery, residue) = BatteryBehaviour.update(battery, 2.0.kw, 1.hour)

    newBattery.state.currentCharge shouldBe 7.0.kwh
    residue shouldBe 0.0.kwh
  }

  it should "handle excess energy when hitting maxPowerCharge limit" in {
    val battery = Battery(spec, BatteryState(5.0.kwh))
    val (newBattery, residue) = BatteryBehaviour.update(battery, 10.0.kw, 1.hour)

    newBattery.state.currentCharge shouldBe 10.0.kwh
    residue shouldBe 5.0.kwh
  }

  it should "handle excess energy when hitting capacity limit" in {
    val battery = Battery(spec, BatteryState(9.0.kwh))
    val (newBattery, residue) = BatteryBehaviour.update(battery, 5.0.kw, 1.hour)

    newBattery.state.currentCharge shouldBe 10.0.kwh
    residue shouldBe 4.0.kwh
  }

  it should "discharge correctly when within limits" in {
    val battery = Battery(spec, BatteryState(5.0.kwh))
    val (newBattery, residue) = BatteryBehaviour.update(battery, -2.0.kw, 1.hour)

    newBattery.state.currentCharge shouldBe 3.0.kwh
    residue shouldBe Energy.Zero
  }

  it should "handle deficit when hitting maxPowerDischarge limit" in {
    val battery = Battery(spec, BatteryState(10.0.kwh))
    val (newBattery, residue) = BatteryBehaviour.update(battery, -10.0.kw, 1.hour)

    newBattery.state.currentCharge shouldBe 5.0.kwh
    residue shouldBe -5.0.kwh
  }

  it should "handle deficit when hitting minSoC limit" in {
    val battery = Battery(spec, BatteryState(3.0.kwh))
    val (newBattery, residue) = BatteryBehaviour.update(battery, -5.0.kw, 1.hour)

    newBattery.state.currentCharge shouldBe 2.0.kwh
    residue shouldBe -4.0.kwh
  }
}
