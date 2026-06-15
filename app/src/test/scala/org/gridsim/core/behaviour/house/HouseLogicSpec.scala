package org.gridsim.core.behaviour.house

import org.gridsim.core.model.*
import org.gridsim.core.model.house.*
import org.gridsim.core.model.battery.*
import org.gridsim.core.common.Units.*
import org.gridsim.core.behaviour.EnergyResolver.*
import org.gridsim.core.behaviour.house.HouseLogic.given
import org.gridsim.core.behaviour.battery.BatteryLogic.given
import org.gridsim.core.common.GeographicPoint
import org.gridsim.core.common.Ticks.Tick
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class HouseLogicSpec extends AnyFlatSpec with Matchers {

  trait TestEnv extends Environment {
    override def tick: Tick = Tick.start
    override def hour: Int = 12
    override def delta: FiniteDuration = 1.hour
    override def weather(point: GeographicPoint): WeatherConditions = ???

    override def update(): Environment = ???
  }

  def mockEnv(h: Int = 11, d: FiniteDuration = 1.hour): Environment = new TestEnv {
    override def hour: Int = h
    override def delta: FiniteDuration = d
  }

  given delta: FiniteDuration = 1.hour

  "HouseLogic" should "orchestrate internal consumption based on size and occupancy" in {
    val house = House.makeEmptyHouse("House1", Size.Small, Occupancy.Traditional).getOrElse(fail())

    val (_, residue) = house.resolve(mockEnv(h = 11))

    residue shouldBe Flow.Deficit(2.0.kwh)
  }

  it should "scale consumption correctly for different house sizes" in {
    val house = House.makeEmptyHouse("House2", Size.Large, Occupancy.Traditional).getOrElse(fail())

    val (_, residue) = house.resolve(mockEnv(h = 11))

    residue shouldBe Flow.Deficit(4.0.kwh)
  }

  it should "integrate with storages (batteries) to cover the calculated deficit" in {
    val spec = BatterySpecification(10.kwh, 5.kw, 5.kw, 0.0)
    val battery = Battery("Battery1", spec, BatteryState(5.kwh))
    val house = House.makeHouseWithStorages("House3", Size.Small, Occupancy.Traditional, List(battery)).getOrElse(fail())

    val (newHouse, residue) = house.resolve(mockEnv(h = 11))

    residue shouldBe Flow.Balanced
    val finalBattery = newHouse.storages.head.asInstanceOf[Battery]
    finalBattery.state.currentCharge shouldBe 3.0.kwh
  }

  it should "thread energy flow sequentially through multiple batteries" in {
    val spec = BatterySpecification(10.kwh, 5.kw, 5.kw, 0.0)
    val b1 = Battery("Battery1", spec, BatteryState(1.kwh))
    val b2 = Battery("Battery2", spec, BatteryState(5.kwh))
    val house = House.makeHouseWithStorages("House4", Size.Large, Occupancy.Traditional, List(b1, b2)).getOrElse(fail())

    val (newHouse, residue) = house.resolve(mockEnv(h = 11))

    residue shouldBe Flow.Balanced
    newHouse.storages.head.asInstanceOf[Battery].state.currentCharge shouldBe 0.kwh
    newHouse.storages.last.asInstanceOf[Battery].state.currentCharge shouldBe 2.kwh
  }

  it should "result in a Balanced flow if the simulation tick duration is zero" in {
    val house = House.makeEmptyHouse("House6", Size.Small, Occupancy.Traditional).getOrElse(fail())
    given delta: FiniteDuration = 0.hour
    val (_, residue) = house.resolve(mockEnv(d = 0.seconds))

    residue shouldBe Flow.Balanced
  }
}
