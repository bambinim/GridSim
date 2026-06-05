package org.gridsim.core.model

import cats.implicits.*
import org.gridsim.core.behaviour.EnergyResolver.*
import org.gridsim.core.behaviour.EnergyResolverSyntax.solve
import org.gridsim.core.model.Occupancy.Traditional
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Tick.Tick
import org.gridsim.core.model.battery.{Battery, BatterySpecification, BatteryState}
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class HouseSpec extends AnyFlatSpec with Matchers {

  "A House" should "calculate correctly its base energy request" in {
    val result = House.makeHouse("House 1", Size.Large, Traditional)
    val env = new Environment:
      override def tick: Tick = ???
      override def hour: Int = 11
      override def delta: FiniteDuration = 1.hour
      override def irradiance(point: GeographicPoint): WeatherConditions = ???
      override def update(): Unit = ???

    val house = result.getOrElse(fail("Validation failed"))

    val (newHouse, energyRequest) = house.solve(env)
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
    val battery = Battery(spec, state)
    val components = List(HouseComponent.BatteryComponent(battery))

    val result = House.makeHouse("House 1", Size.Large, Traditional, components)
    val env = new Environment:
      override def tick: Tick = ???
      override def hour: Int = 11
      override def delta: FiniteDuration = 1.hour
      override def irradiance(point: GeographicPoint): WeatherConditions = ???
      override def update(): Unit = ???

    val house = result.getOrElse(fail("Validation failed"))

    val (newHouse, energy) = house.solve(env)
    
    // Total consumption: 4kWh. Battery can discharge 2kWh. Residual Deficit: 2kWh.
    energy shouldBe Flow.Deficit(2.0.kwh)

    // Verify battery state updated
    val finalBattery = newHouse.components.head match
      case HouseComponent.BatteryComponent(b) => b

    finalBattery.state.currentCharge shouldBe 3.0.kwh
  }

  it should "fail validation if the ID is too short" in {
    val result = House.makeHouse("H1", Size.Small, Traditional)
    result.isInvalid shouldBe true
  }
}
