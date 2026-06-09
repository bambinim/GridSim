package org.gridsim.core.model

import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Tick.Tick
import org.gridsim.core.model.battery.*
import org.gridsim.core.behaviour.*
import org.gridsim.core.behaviour.battery.BatteryLogic.given
import org.gridsim.core.behaviour.EnergyResolver.*
import org.gridsim.core.behaviour.house.ConsumptionProfile
import org.gridsim.core.validation.Validator
import org.gridsim.core.validation.BatteryValidator.given
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class BatterySpec extends AnyFlatSpec with Matchers {

  val env = new Environment:
    override def tick: Tick = ???

    override def hour: Int = 11

    override def delta: FiniteDuration = 1.hour

    override def irradiance(point: GeographicPoint): WeatherConditions = ???

    override def update(): Unit = ???

  val spec = BatterySpecification(
    capacity = 10.0.kwh,
    maxPowerCharge = 5.0.kw,
    maxPowerDischarge = 5.0.kw,
    minSoC = 0.2
  )

  "A Battery" should "be correctly initialized with its specs and state" in {
    val state = BatteryState(currentCharge = 5.0.kwh)
    val result = Battery.make(spec, state)
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
    val result = Battery.make(specEr, state)
    result.isInvalid shouldBe true
  }

  it should "be invalid if state is invalid" in {
    val state = BatteryState(currentCharge = 20.kwh)
    val result = Battery.make(spec, state)
    result.isInvalid shouldBe true
  }

  it should "calculate correctly its battery level" in {
    val battery = Battery(spec, BatteryState(5.kwh))
    battery.getBatteryLevel shouldBe 0.5
  }

  it should "handle extreme power/energy values without crashing" in {
    val extremeSpec = BatterySpecification(1e-10.kwh, 1e10.kw, 1e10.kw, 0.0)
    val battery = Battery(extremeSpec, BatteryState(0.kwh))

    val (newBattery, residue) = battery.runSolve(Flow.Surplus(1.kwh), env)
    newBattery.state.currentCharge shouldBe extremeSpec.capacity
  }

  it should "not charge if already at capacity" in {
    val fullBattery = Battery(spec, BatteryState(10.0.kwh))
    val (newBattery, residue) = fullBattery.runSolve(Flow.Surplus(1.0.kwh), env)

    newBattery.state.currentCharge shouldBe 10.0.kwh
    residue shouldBe Flow.Surplus(1.0.kwh)
  }

  it should "not discharge if already at minSoC" in {
    val emptyBattery = Battery(spec, BatteryState(2.0.kwh))
    val (newBattery, residue) = emptyBattery.runSolve(Flow.Deficit(1.0.kwh), env)

    newBattery.state.currentCharge shouldBe 2.0.kwh
    residue shouldBe Flow.Deficit(1.0.kwh)
  }

  "BatteryLogic" should "charge correctly when within limits" in {
    val battery = Battery(spec, BatteryState(5.0.kwh))
    val (newBattery, residue) = battery.runSolve(Flow.Surplus(2.0.kwh), env)

    newBattery.state.currentCharge shouldBe 7.0.kwh
    residue shouldBe Flow.Balanced
  }

  it should "handle excess energy when hitting maxPowerCharge limit" in {
    val battery = Battery(spec, BatteryState(5.0.kwh))
    val (newBattery, residue) = battery.runSolve(Flow.Surplus(10.0.kwh), env)

    newBattery.state.currentCharge shouldBe 10.0.kwh
    residue shouldBe Flow.Surplus(5.0.kwh)
  }

  it should "handle excess energy when hitting capacity limit" in {
    val battery = Battery(spec, BatteryState(9.0.kwh))
    val (newBattery, residue) = battery.runSolve(Flow.Surplus(5.0.kwh), env)

    newBattery.state.currentCharge shouldBe 10.0.kwh
    residue shouldBe Flow.Surplus(4.0.kwh)
  }

  it should "discharge correctly when within limits" in {
    val battery = Battery(spec, BatteryState(5.0.kwh))
    val (newBattery, residue) = battery.runSolve(Flow.Deficit(2.0.kwh), env)

    newBattery.state.currentCharge shouldBe 3.0.kwh
    residue shouldBe Flow.Balanced
  }

  it should "handle deficit when hitting maxPowerDischarge limit" in {
    val battery = Battery(spec, BatteryState(10.0.kwh))
    val (newBattery, residue) = battery.runSolve(Flow.Deficit(10.0.kwh), env)

    newBattery.state.currentCharge shouldBe 5.0.kwh
    residue shouldBe Flow.Deficit(5.0.kwh)
  }

  it should "handle deficit when hitting minSoC limit" in {
    val battery = Battery(spec, BatteryState(3.0.kwh))
    val (newBattery, residue) = battery.runSolve(Flow.Deficit(5.0.kwh), env)

    newBattery.state.currentCharge shouldBe 2.0.kwh
    residue shouldBe Flow.Deficit(4.0.kwh)
  }
}
