package org.gridsim.core.behaviour.battery

import org.gridsim.core.common.Flow.*
import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.*
import org.gridsim.core.model.*
import org.gridsim.core.model.battery.*
import org.gridsim.core.behaviour.EnergyExchanger.*
import org.gridsim.core.behaviour.battery.BatteryLogic.given
import org.gridsim.core.common.GeographicPoint
import org.gridsim.core.common.Ticks.Tick
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class BatteryLogicSpec extends AnyFlatSpec with Matchers {

  trait TestEnv extends Environment {
    override def tick: Tick = Tick.start
    override def hour: Int = 12
    override def delta: FiniteDuration = 1.hour
    override def weather(point: GeographicPoint): WeatherConditions = ???

    override def update(): Environment = ???
  }

  val spec = BatterySpecification(10.kwh, 5.kw, 5.kw, 0.2)

  given delta: FiniteDuration = 1.hour

  "BatteryLogic" should "dispatch surplus flow to charging strategy" in {
    val battery = Battery("Battery 1", spec, BatteryState(0.kwh))
    val env = new TestEnv {}

    val (newBattery, residue) = battery.exchange(Surplus(10.kwh), env)

    newBattery.state.currentCharge.toDouble shouldBe 5.0
    residue shouldBe Surplus(5.kwh)
    }

    it should "dispatch deficit flow to discharging strategy" in {
    val battery = Battery("Battery 1", spec, BatteryState(10.kwh))
    val env = new TestEnv {}

    val (newBattery, residue) = battery.exchange(Deficit(10.kwh), env)

    newBattery.state.currentCharge.toDouble shouldBe 5.0
    residue shouldBe Deficit(5.kwh)
    }

    it should "return Balanced residue when flow is balanced" in {
    val battery = Battery("Battery 1", spec, BatteryState(5.kwh))
    val env = new TestEnv {}

    val (newBattery, residue) = battery.exchange(Balanced, env)

    newBattery.state.currentCharge.toDouble shouldBe 5.0
    residue shouldBe Balanced
    }
}
