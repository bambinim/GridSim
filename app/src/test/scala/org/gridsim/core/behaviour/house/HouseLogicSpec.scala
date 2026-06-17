package org.gridsim.core.behaviour.house

import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.gridsim.core.model.*
import org.gridsim.core.model.house.*
import org.gridsim.core.model.battery.*
import org.gridsim.core.behaviour.house.HouseEvolution.*
import org.gridsim.core.behaviour.battery.BatteryEnergyExchange.*
import org.gridsim.core.behaviour.shaping.{DemandShaper, GaussianShaper, IdentityShaper}
import org.gridsim.core.common.GeographicPoint
import org.gridsim.core.common.StochasticGenerator
import org.gridsim.core.validation.HouseComponentValidator.given
import org.gridsim.core.behaviour.GridEvolution
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class HouseLogicSpec extends AnyFlatSpec with Matchers {

  private val env = new Environment {
    override def time: SimulationTime = SimulationTime(0, 0, 11, 0)
    override def weather(point: GeographicPoint): WeatherConditions = ???
    override def advance(delta: FiniteDuration): Environment = ???
  }

  given delta: FiniteDuration = 1.hour
  given ConsumptionResolver = new StochasticConsumptionResolver()

  "HouseLogic with IdentityShaper" should "produce exact deterministic consumption" in {
    // Inject IdentityShaper for predictability
    given shaper: DemandShaper = IdentityShaper()


    val entity = House("HouseDet", Nil)
    val state = HouseState("HouseDet", Nil)

    val (_, residue) = state.evolve(entity, env)

    val Flow.Deficit(amt) = residue: @unchecked
    amt.toDouble should be(0.5 +- 0.01)
  }

  it should "integrate correctly with batteries using deterministic flow" in {
    given shaper: DemandShaper = IdentityShaper()
    val battery = Battery(
      id = "Battery1",
      maxCapacity = 10.kwh,
      maxPowerCharge = 5.kw,
      maxPowerDischarge = 5.kw,
      minSoC = 0.0
    )
    val bState = BatteryState("Battery1", 5.kwh)

    val entity = House("HouseBat", List(battery))
    val state = HouseState("HouseBat", List(bState))

    val (newState, residue) = state.evolve(entity, env)

    // DEBUG: Stampa cosa hai ottenuto
    println(s"DEBUG: Residue è $residue")

    // 0.5 kWh deficit covered by 5 kWh battery -> Balanced residue, 4.5 kWh charge
    residue shouldBe Flow.Balanced
    val finalBatteryState = newState.componentStates.head.asInstanceOf[BatteryState]
    finalBatteryState.currentCharge.toDouble shouldBe 4.5
  }

  "HouseLogic with GaussianShaper" should "produce stochastic consumption within reasonable bounds" in {
    // Inject GaussianShaper with a fixed seed for minimal reproducibility in this test
    val gen = StochasticGenerator.fromSeed(12345L)
    given shaper: DemandShaper = GaussianShaper(gen)

    val entity = House("HouseStoch", Nil)
    val state = HouseState("HouseStoch", Nil)

    val (_, res1) = state.evolve(entity, env)
    val (_, res2) = state.evolve(entity, env)

    // Stochastic values should differ
    res1 should not be res2

    // Values should still be sensible (mean 0.5 kW, variance 0.1)
    val Flow.Deficit(amt1) = res1: @unchecked
    amt1.toDouble should (be > 0.0 and be < 1.0)
  }

  it should "result in a Balanced flow if the simulation tick duration is zero regardless of shaper" in {
    given shaper: DemandShaper = IdentityShaper()
    given zeroDelta: FiniteDuration = 0.hour

    val entity = House("HouseZero", Nil)
    val state = HouseState("HouseZero", Nil)

    val (_, residue) = state.evolve(entity, env)

    residue shouldBe Flow.Balanced
  }
}
