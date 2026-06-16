package org.gridsim.core.behaviour.house

import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.gridsim.core.model.*
import org.gridsim.core.model.house.*
import org.gridsim.core.model.battery.*
import org.gridsim.core.behaviour.EnergyResolver.*
import org.gridsim.core.behaviour.house.HouseLogic.given
import org.gridsim.core.behaviour.battery.BatteryLogic.given
import org.gridsim.core.behaviour.shaping.{DemandShaper, GaussianShaper, IdentityShaper}
import org.gridsim.core.common.GeographicPoint
import org.gridsim.core.common.Ticks.Tick
import org.gridsim.core.common.StochasticGenerator
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
  "HouseLogic with IdentityShaper" should "produce exact deterministic consumption" in {
    // Inject IdentityShaper for predictability
    given shaper: DemandShaper = IdentityShaper()
    val house = House.makeEmptyHouse("HouseDet").getOrElse(fail())

    val (_, residue) = house.resolve(mockEnv(h = 11))

    // Hour 11 in TraditionalProfile has mean 0.5 kW -> 0.5 kWh
    residue shouldBe Flow.Deficit(0.5.kwh)
  }

  it should "integrate correctly with batteries using deterministic flow" in {
    given shaper: DemandShaper = IdentityShaper()
    val spec = BatterySpecification(10.kwh, 5.kw, 5.kw, 0.0)
    val battery = Battery("Battery1", spec, BatteryState(5.kwh))
    val house = House.makeHouseWithStorages[List]("HouseBat", List(battery)).getOrElse(fail())

    val (newHouse, residue) = house.resolve(mockEnv(h = 11))

    // 0.5 kWh deficit covered by 5 kWh battery -> Balanced residue, 4.5 kWh charge
    residue shouldBe Flow.Balanced
    val finalBattery = newHouse.storages.head.asInstanceOf[Battery]
    finalBattery.state.currentCharge.toDouble shouldBe 4.5
  }

  "HouseLogic with GaussianShaper" should "produce stochastic consumption within reasonable bounds" in {
    // Inject GaussianShaper with a fixed seed for minimal reproducibility in this test
    val gen = StochasticGenerator.fromSeed(12345L)
    given shaper: DemandShaper = GaussianShaper(gen)
    val house = House.makeEmptyHouse("HouseStoch").getOrElse(fail())

    val (_, res1) = house.resolve(mockEnv(h = 11))
    val (_, res2) = house.resolve(mockEnv(h = 11))

    // Stochastic values should differ
    res1 should not be res2

    // Values should still be sensible (mean 0.5 kW, variance 0.1)
    val Flow.Deficit(amt1) = res1: @unchecked
    amt1.toDouble should (be > 0.0 and be < 1.0)
  }

  it should "result in a Balanced flow if the simulation tick duration is zero regardless of shaper" in {
    given shaper: DemandShaper = IdentityShaper()
    given delta: FiniteDuration = 0.hour
    val house = House.makeEmptyHouse("HouseZero").getOrElse(fail())
    val (_, residue) = house.resolve(mockEnv(d = 0.seconds))

    residue shouldBe Flow.Balanced
  }
}
