package org.gridsim.core.behaviour.battery

import org.gridsim.core.common.Flow.*
import org.gridsim.core.common.Energy.*
import org.gridsim.core.common.*
import org.gridsim.core.model.*
import org.gridsim.core.behaviour.storage.StorageEnergyExchanger.exchange
import org.gridsim.core.behaviour.storage.battery.BatteryEnergyExchange.given
import org.gridsim.core.behaviour.storage.StorageEnergyExchanger
import org.gridsim.core.common.GeographicPoint
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.time.LocalDateTime
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class BatteryEnergyExchangeSpec extends AnyFlatSpec with Matchers {

  private val env = new Environment {
    override def startDateTime: LocalDateTime = LocalDateTime.now
    override def time: FiniteDuration = 11.hours
    override def weather(point: GeographicPoint): WeatherConditions = ???
    override def advance(delta: FiniteDuration): Environment = ???
  }

  private val battery = Battery(
    id = "Battery 1",
    maxCapacity = 10.kwh,
    maxPowerCharge = 5.kw,
    maxPowerDischarge = 5.kw,
    minSoC = 0.2
  )

  given delta: FiniteDuration = 1.hour

  "BatteryEnergyExchange" should "dispatch surplus flow to charging strategy" in {
    val state = BatteryState("Battery 1", 0.kwh)


    val (newState, residue) = state.exchange(battery, Surplus(10.kwh), env)

    newState.currentCharge.toDouble shouldBe 5.0
    residue shouldBe Surplus(5.kwh)
  }

  it should "dispatch deficit flow to discharging strategy" in {
    val state = BatteryState("Battery 1", 10.kwh)

    val (newState, residue) = state.exchange(battery, Deficit(10.kwh), env)

    newState.currentCharge.toDouble shouldBe 5.0
    residue shouldBe Deficit(5.kwh)
  }

  it should "return Balanced residue when flow is balanced" in {
    val state = BatteryState("Battery 1", 5.kwh)

    val (newState, residue) = state.exchange(battery, Balanced, env)

    newState.currentCharge.toDouble shouldBe 5.0
    residue shouldBe Balanced
  }
}
