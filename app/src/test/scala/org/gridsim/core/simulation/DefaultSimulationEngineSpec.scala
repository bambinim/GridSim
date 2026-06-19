package org.gridsim.core.simulation

import org.gridsim.core.behaviour.house.{ConsumptionResolver, StochasticConsumptionResolver}
import org.gridsim.core.behaviour.shaping.{DemandShaper, IdentityShaper}
import org.gridsim.core.common.{Energy, Flow, kw, kwh}
import org.gridsim.core.model.Environment
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.network.{Cable, CableConnections, ExternalGrid, GridGraph}
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.solver.{PowerFlowSolver, SimplePowerFlowSolver}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.collection.Map
import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class DefaultSimulationEngineSpec extends AnyFlatSpec with Matchers:

  private val graph =
    GridGraph(
      nodes = List(ExternalGrid("external-grid")),
      cables = Nil
    )

  private val model = SimulationModel(graph, 15.minutes)

  given ConsumptionResolver = new StochasticConsumptionResolver()
  given DemandShaper = IdentityShaper()
  given PowerFlowSolver = SimplePowerFlowSolver(graph)

  private val engine = DefaultSimulationEngine(model)

  it should "advance the environment by the model delta" in:
    val current =
      SimulationState(
        environment = Environment(2.hours),
        entityStates = Nil
      )

    val next = engine.step(current)

    next.environment.time shouldBe 2.hours + 15.minutes

  it should "resolve every grid entity state" in:

    val battery =
      Battery(
        id = "battery-1",
        maxCapacity = 2.kwh,
        maxPowerCharge = 1.kw,
        maxPowerDischarge = 1.kw,
        minSoC = 0.2
      )
    val batteryState = BatteryState("battery-1", currentCharge = 1.kwh)
    val house = House("house-1", components = List(battery))
    val houseState =
      HouseState("house-1", componentStates = List(batteryState))
    val grid = GridGraph(
      nodes = List(ExternalGrid("external-grid"), house),
      cables = Nil
    )
    val engine =
      DefaultSimulationEngine(
        SimulationModel(grid, 15.minutes)
      )(using summon, SimplePowerFlowSolver(grid))
    val current =
      SimulationState(
        environment = Environment(2.hours),
        entityStates = List(houseState)
      )

    val next = engine.step(current)

    val nextBatteryCharge =
      next.entityStates
        .collectFirst { case state: HouseState => state }
        .flatMap(
          _.componentStates
            .collectFirst { case state: BatteryState => state.currentCharge }
        )

    nextBatteryCharge shouldBe Some(0.9625.kwh)
    next.entityFlows.get(house.id) shouldBe Some(Flow.Balanced)

  it should "calculate the load on every cable" in:
    val externalGrid = ExternalGrid("external-grid")
    val house = House("house-1", components = Nil)
    val houseState = HouseState("house-1", componentStates = Nil)
    val cable =
      Cable(
        CableConnections(externalGrid.id, house.id),
        maxCapacity = 1.kwh
      )
    val grid =
      GridGraph(
        nodes = List(externalGrid, house),
        cables = List(cable)
      )
    val engine =
      DefaultSimulationEngine(
        SimulationModel(grid, 15.minutes)
      )(using summon, SimplePowerFlowSolver(grid))
    val current =
      SimulationState(
        environment = Environment(2.hours),
        entityStates = List(houseState)
      )

    val next = engine.step(current)

    next.cableLoads.get(cable) shouldBe Some(0.0375.kwh)
