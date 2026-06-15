package org.gridsim.core.model

import cats.implicits.*
import org.gridsim.core.behaviour.EnergyResolver.*
import org.gridsim.core.behaviour.house.HouseLogic.given
import org.gridsim.core.behaviour.battery.BatteryLogic.given
import org.gridsim.core.model.house.Occupancy.Traditional
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Tick
import org.gridsim.core.model.battery.{Battery, BatterySpecification, BatteryState}
import org.gridsim.core.model.house.{House, Size}

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class HouseSpec extends AnyFlatSpec with Matchers {

  "A House" should "calculate correctly its base energy request" in {
    val result = House.makeEmptyHouse("House 1", Size.Large, Traditional)
    val env = new Environment:
      override def tick: Tick = ???
      override def hour: Int = 11
      override def delta: FiniteDuration = 1.hour
      override def irradiance(point: GeographicPoint): WeatherConditions = ???
      override def update(): Unit = ???

    val house = result.getOrElse(fail("Validation failed"))

    val (newHouse, energyRequest) = house.runSolve(env)
    energyRequest shouldBe Flow.Deficit(4.0.kwh)
  }

  it should "handle a battery component to cover part of the deficit" in {
    val spec = BatterySpecification(
      capacity = 10.0.kwh,
      maxPowerCharge = 5.0.kw,
      maxPowerDischarge = 2.0.kw,
      minSoC = 0.2
    )
    val state = BatteryState(currentCharge = 5.0.kwh)
    val battery = Battery("Battery 1", spec, state)
    val components = List(battery)

    val result = House.makeHouseWithStorages("House 1", Size.Large, Traditional, components)
    val env = new Environment:
      override def tick: Tick = ???
      override def hour: Int = 11
      override def delta: FiniteDuration = 1.hour
      override def irradiance(point: GeographicPoint): WeatherConditions = ???
      override def update(): Unit = ???

    val house = result.getOrElse(fail("Validation failed"))

    val (newHouse, energy) = house.runSolve(env)

    // Total consumption: 4kWh. Battery can discharge 2kWh. Residual Deficit: 2kWh.
    energy shouldBe Flow.Deficit(2.0.kwh)

    // Verify battery state updated
    val finalBattery = newHouse.storages.head match
      case b: Battery => b
      case _ => fail("Should be a battery")

    finalBattery.state.currentCharge shouldBe 3.0.kwh
  }

  it should "handle multiple batteries in sequence threading the flow" in {
    val spec = BatterySpecification(10.kwh, 5.kw, 5.kw, 0.0)
    val b1 = Battery("B1", spec, BatteryState(1.kwh))
    val b2 = Battery("B2", spec, BatteryState(1.kwh))
    val components = List(b1, b2)

    val result = House.makeHouseWithStorages("MultiBattery", Size.Large, Traditional, components)
    val house = result.getOrElse(fail("Validation failed"))

    val env = new Environment:
      override def tick: Tick = ???
      override def hour: Int = 11 // Consumption 4.0 kWh
      override def delta: FiniteDuration = 1.hour
      override def irradiance(point: GeographicPoint): WeatherConditions = ???
      override def update(): Unit = ???

    // 4.0 deficit. B1 gives 1.0 (empty). B2 gives 1.0 (empty). Residual 2.0.
    val (updatedHouse, residue) = house.runSolve(env)

    residue shouldBe Flow.Deficit(2.0.kwh)
    updatedHouse.storages.foreach {
      case b: Battery => b.state.currentCharge shouldBe 0.kwh
      case _ => fail("Should be a battery")
    }
  }

  it should "correctly charge batteries when external surplus is injected" in {
    val b = Battery("B1", BatterySpecification(10.kwh, 5.kw, 5.kw, 0.0), BatteryState(0.kwh))
    val result = House.makeHouseWithStorages("SurplusHouse", Size.Small, Traditional, List(b))
    val house = result.getOrElse(fail("Validation failed"))
    val env = new Environment:
      override def tick: Tick = ???
      override def hour: Int = 11 // Consumption 2.0 kWh
      override def delta: FiniteDuration = 1.hour
      override def irradiance(point: GeographicPoint): WeatherConditions = ???
      override def update(): Unit = ???

    // Inject 10kWh surplus. House consumes 2kWh -> 8kWh left for battery.
    // Battery capacity 10, max charge 5. So battery takes 5. Residue 3 Surplus.
    val (updatedHouse, residue) = house.runSolve(Flow.Surplus(10.kwh), env)

    residue shouldBe Flow.Surplus(3.0.kwh)
    val finalBattery = updatedHouse.storages.head match
      case b: Battery => b
      case _ => fail("Should be a battery")
    finalBattery.state.currentCharge shouldBe 5.0.kwh
  }

  it should "not crash with zero-duration ticks" in {
    val result = House.makeHouseWithStorages("ZeroTick", Size.Small, Traditional, Seq(Battery(
      "B1", BatterySpecification(10.kwh, 5.kw, 5.kw, 0.0), BatteryState(5.kwh)
    )))
    val house = result.getOrElse(fail("Validation failed"))
    val env = new Environment:
      override def tick: Tick = ???
      override def hour: Int = 11
      override def delta: FiniteDuration = 0.seconds
      override def irradiance(point: GeographicPoint): WeatherConditions = ???
      override def update(): Unit = ???

    val (updatedHouse, residue) = house.runSolve(env)
    residue shouldBe Flow.Balanced // 0 duration = 0 energy
  }

  it should "work with an empty components list" in {
    val result = House.makeHouseWithStorages("Empty", Size.Small, Traditional, Nil)
    val house = result.getOrElse(fail("Validation failed"))
    val env = new Environment:
      override def tick: Tick = ???
      override def hour: Int = 11 // 2.0 kWh
      override def delta: FiniteDuration = 1.hour
      override def irradiance(point: GeographicPoint): WeatherConditions = ???
      override def update(): Unit = ???

    val (updatedHouse, residue) = house.runSolve(env)
    residue shouldBe Flow.Deficit(2.0.kwh)
  }

  it should "fail validation if the ID is too short" in {
    val result = House.makeEmptyHouse("H1", Size.Small, Traditional)
    result.isInvalid shouldBe true
  }
}
