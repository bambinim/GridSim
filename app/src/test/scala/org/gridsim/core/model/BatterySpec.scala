package org.gridsim.core.model

import org.gridsim.core.common.*
import org.gridsim.core.model.battery.*
import org.gridsim.core.behaviour.*
import org.gridsim.core.behaviour.battery.BatteryLogic.given
import org.gridsim.core.behaviour.EnergyResolver.*
import org.gridsim.core.validation.BatteryValidator.given
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.FiniteDuration

@RunWith(classOf[JUnitRunner])
class BatterySpec extends AnyFlatSpec with Matchers {

  val env = Environment(11.hours)

  val spec = BatterySpecification(
    capacity = 10.kwh,
    maxPowerCharge = 5.kw,
    maxPowerDischarge = 5.kw,
    minSoC = 0.2
  )

  given delta: FiniteDuration = 1.hour

  "A Battery" should "be correctly initialized with its specs and state" in {
    val state = BatteryState(currentCharge = 5.kwh)
    val result = Battery.make("Battery 1", spec, state)

    result.isValid shouldBe true
    val battery = result.getOrElse(fail())
    battery.id shouldBe "Battery 1"
    battery.spec shouldBe spec
    battery.state shouldBe state
  }

  it should "calculate correctly its battery level" in {
    val battery = Battery("B1", spec, BatteryState(5.kwh))
    battery.percentage shouldBe 0.5
  }

  it should "report 1.0 when full" in {
    val battery = Battery("Full", spec, BatteryState(10.kwh))
    battery.percentage shouldBe 1.0
  }

  it should "report 0.0 when empty" in {
    val battery = Battery("Empty", spec, BatteryState(0.kwh))
    battery.percentage shouldBe 0.0
  }
}