package org.gridsim.core.behaviour.house

import org.gridsim.core.common.*
import org.gridsim.core.model.*
import org.gridsim.core.model.house.*
import org.gridsim.core.behaviour.house.HouseEvolution.*
import org.gridsim.core.behaviour.storage.StorageEnergyExchanger
import org.gridsim.core.behaviour.shaping.{DemandShaper, IdentityShaper}
import org.gridsim.core.behaviour.EvolutionContext
import org.gridsim.core.common.GeographicPoint
import org.gridsim.core.validation.HouseComponentValidator.given
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.validation.SolarPanelValidator.given
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.time.LocalDateTime
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class HouseLogicSpec extends AnyFlatSpec with Matchers {

  private val env = new Environment {
    override def startDateTime: LocalDateTime = LocalDateTime.of(2026, 7, 11, 0, 0)
    override def time: FiniteDuration = 11.hours
    override def weather(point: GeographicPoint): WeatherConditions = new WeatherConditions {
      override def irradiance: Irradiance = Irradiance.Zero
      override def temperature: Temperatures.AnyTemperature = Temperatures.Temperature.celsius(25.0).toAny
    }
    override def advance(delta: FiniteDuration): Environment = this
  }

  "HouseLogic" should "use the hour of day after a day rollover" in {
    var resolvedHour: Option[Long] = None
    val mockResolver = new ConsumptionResolver:
      override def resolve(hour: Long, strategy: ConsumptionStrategy)(using delta: FiniteDuration, shaper: DemandShaper): Flow[Energy] =
        resolvedHour = Some(hour)
        Flow.Deficit(Energy(0.2))

    given shaper: DemandShaper = IdentityShaper()
    given context: EvolutionContext[HouseEvolutionDependencies] =
      EvolutionContext(1.hour, HouseEvolutionDependencies(mockResolver, shaper))

    val env = Environment(2.days)
    val entity = House("HouseDet", Nil)
    val state = HouseState("HouseDet", Nil)

    val (_, residue) = state.evolve(entity, env)

    resolvedHour shouldBe Some(0)
    residue shouldBe Flow.Deficit(Energy(0.2))
  }

  it should "integrate correctly with batteries using deterministic flow from the resolver" in {
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

    // Mock resolver to return a deficit of 2.0 kWh
    val mockResolver = new ConsumptionResolver:
      override def resolve(hour: Long, strategy: ConsumptionStrategy)(using delta: FiniteDuration, shaper: DemandShaper): Flow[Energy] =
        Flow.Deficit(Energy(2.0))

    given shaper: DemandShaper = IdentityShaper()
    given context: EvolutionContext[HouseEvolutionDependencies] =
      EvolutionContext(1.hour, HouseEvolutionDependencies(mockResolver, shaper))

    val (newState, residue) = state.evolve(entity, env)

    // 2.0 kWh deficit covered by 5.0 kWh battery -> Balanced residue, 3.0 kWh charge
    residue shouldBe Flow.Balanced
    val finalBatteryState = newState.componentStates.head.asInstanceOf[BatteryState]
    finalBatteryState.currentCharge.toDouble shouldBe 3.0
  }

  it should "include solar panel production before resolving the final house flow" in {
    val location = GeographicPoint(44.3, 11.7)
    val (panel, panelState) =
      SolarPanel(
        id = "Panel1",
        location = location,
        maxProduction = 5.kw,
        areaSqm = 20.0,
        efficiency = 0.20
      ).toOption.get
    val entity = House("HouseSolar", List(panel))
    val state = HouseState("HouseSolar", List(panelState))

    val mockResolver = new ConsumptionResolver:
      override def resolve(hour: Long, strategy: ConsumptionStrategy)(using delta: FiniteDuration, shaper: DemandShaper): Flow[Energy] =
        Flow.Deficit(Energy(5.0))

    given shaper: DemandShaper = IdentityShaper()
    given context: EvolutionContext[HouseEvolutionDependencies] =
      EvolutionContext(1.hour, HouseEvolutionDependencies(mockResolver, shaper))

    val mockEnv = new Environment:
      override def startDateTime: LocalDateTime = LocalDateTime.of(2026, 7, 11, 0, 0)
      override def time: FiniteDuration = 6.hours
      override def weather(point: GeographicPoint): WeatherConditions = new WeatherConditions:
        override def irradiance: Irradiance = 1000.0.wm2
        override def temperature: Temperatures.AnyTemperature = Temperatures.Temperature.celsius(25.0).toAny
      override def advance(delta: FiniteDuration): Environment = this

    val (newState, residue) = state.evolve(entity, mockEnv)

    residue shouldBe Flow.Deficit(Energy(1.0))
    val finalPanelState = newState.componentStates.head.asInstanceOf[SolarPanelState]
    finalPanelState.efficiency shouldBe panel.efficiency
  }

  it should "pass the configured shaper to the resolver" in {
    val expectedShaper: DemandShaper = new DemandShaper:
      override def shape(mean: Double, variance: Double): Double = mean

    var receivedShaper: Option[DemandShaper] = None
    val mockResolver = new ConsumptionResolver:
      override def resolve(hour: Long, strategy: ConsumptionStrategy)(using delta: FiniteDuration, shaper: DemandShaper): Flow[Energy] =
        receivedShaper = Some(shaper)
        Flow.Balanced

    given context: EvolutionContext[HouseEvolutionDependencies] =
      EvolutionContext(1.hour, HouseEvolutionDependencies(mockResolver, expectedShaper))

    val entity = House("HouseStoch", Nil)
    val state = HouseState("HouseStoch", Nil)

    val _ = state.evolve(entity, env)

    receivedShaper shouldBe Some(expectedShaper)
  }

  it should "pass the evolution delta to the resolver" in {
    val expectedDelta = 30.minutes
    var receivedDelta: Option[FiniteDuration] = None
    val mockResolver = new ConsumptionResolver:
      override def resolve(hour: Long, strategy: ConsumptionStrategy)(using delta: FiniteDuration, shaper: DemandShaper): Flow[Energy] =
        receivedDelta = Some(delta)
        Flow.Balanced

    given shaper: DemandShaper = IdentityShaper()
    given context: EvolutionContext[HouseEvolutionDependencies] =
      EvolutionContext(expectedDelta, HouseEvolutionDependencies(mockResolver, shaper))

    val entity = House("HouseZero", Nil)
    val state = HouseState("HouseZero", Nil)

    val _ = state.evolve(entity, env)

    receivedDelta shouldBe Some(expectedDelta)
  }
}
