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
import org.gridsim.core.common.Units
import org.gridsim.core.model.battery.{Battery, BatterySpecification, BatteryState}
import org.gridsim.core.model.{Environment, House, Size, WeatherConditions}

@RunWith(classOf[JUnitRunner])
class HouseSpec extends AnyFlatSpec with Matchers {

  "Base House" should "calculate correctly its energy request" in {
    val result = House.makeBaseHouse("House 1", Size.Large, Traditional)
    val env = new Environment:
      override def tick: Tick = ???
      override def hour: Int = 11
      override def irradiance(point: GeographicPoint): WeatherConditions = ???
      override def update(): Unit = ???

    val house = result.fold(
      errors => fail(s"Validation failed: ${errors.toList.mkString(", ")}"),
      identity
    )

    val (newHouse, energyRequest) = house.solve(env)
    energyRequest shouldBe -4.0.kwh

  }

  it should "fail validation if the ID is too short" in {
    val result = House.makeBaseHouse("H1", Size.Small, Traditional)
    result.isInvalid shouldBe true
  }

  "House With Battery" should "calculate correctly its energy request" in {
    val spec = BatterySpecification(
      capacity = 10.0.kwh,
      maxPowerCharge = 5.0.kw,
      maxPowerDischarge = 2.0.kw,
      minSoC = 0.2
    )
    val state = BatteryState(currentCharge = 5.0.kwh)
    val battery = Battery(spec, state)
    val result = House.makeHouseWithBattery("House 1", Size.Large, Traditional, battery)
    val env = new Environment:
      override def tick: Tick = ???

      override def hour: Int = 11

      override def irradiance(point: GeographicPoint): WeatherConditions = ???

      override def update(): Unit = ???

    val house = result.fold(
      errors => fail(s"Validation failed: ${errors.toList.mkString(", ")}"),
      identity
    )

    val (newHouse, energy) = house.solve(env)
    energy shouldBe 2.0.kwh
  }
}
