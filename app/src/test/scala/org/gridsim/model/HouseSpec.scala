package org.gridsim.model

import org.gridsim.behaviour.*
import org.gridsim.model.Occupancy.Traditional
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import org.gridsim.behaviour.EnergyResolverSyntax.*
import org.gridsim.common.Units
import org.gridsim.common.Units.*
import org.gridsim.common.Units.Tick.Tick

@RunWith(classOf[JUnitRunner])
class UnitsSpec extends AnyFlatSpec with Matchers {

  "Base House" should "be calculate correctly his energy request" in {
    val house = BaseHouse("House 1", Size.Large, Traditional)
    val env = new Environment:
      override def tick: Tick = ???

      override def hour: Int = 11

      override def irradiance(point: GeographicPoint): WeatherConditions = ???

      override def update(): Unit = ???

    house.solve(env) shouldBe 4.0.kwh
  }



}

