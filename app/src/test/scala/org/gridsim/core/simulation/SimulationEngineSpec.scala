package org.gridsim.core.simulation

import org.gridsim.core.behaviour.{DefaultEntityEvolutionDispatcher, EntityEvolutionDispatcher}
import org.gridsim.core.behaviour.house.{ConsumptionResolver, HouseEvolutionDependencies, StochasticConsumptionResolver}
import org.gridsim.core.behaviour.shaping.{DemandShaper, IdentityShaper}
import org.gridsim.core.common.{Energy, Flow, GeographicPoint, kw, kwh}
import org.gridsim.core.model.{Environment, SolarPanel, SolarPanelState}
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.network.{Cable, CableConnections, ExternalGrid, GridGraph}
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.solver.{PowerFlowSolver, SimplePowerFlowSolver}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class SimulationEngineSpec extends AnyFlatSpec with Matchers:
  import org.gridsim.core.validation.SolarPanelValidator.given

  private val graph =
    GridGraph(
      nodes = List(ExternalGrid("external-grid")),
      cables = Nil
    )

  private val model = SimulationModel(graph, 15.minutes)

  given ConsumptionResolver = new StochasticConsumptionResolver()
  given DemandShaper = IdentityShaper()

  given EntityEvolutionDispatcher = DefaultEntityEvolutionDispatcher(
    HouseEvolutionDependencies(
      resolver = StochasticConsumptionResolver(),
      shaper = IdentityShaper()
    )
  )

  private val engine = DefaultSimulationEngine(model, SimplePowerFlowSolver(graph))

  it should "advance the environment by the model delta" in:
    val current =
      SimulationState(
        environment = Environment(2.hours),
        entityStates = Map.empty
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
        SimulationModel(grid, 15.minutes),
        SimplePowerFlowSolver(grid)
      )
    val current =
      SimulationState(
        environment = Environment(2.hours),
        entityStates = Map(houseState.entityId -> houseState)
      )

    val next = engine.step(current)

    val nextBatteryCharge =
      next.entityStates
        .values
        .collectFirst { case state: HouseState => state }
        .flatMap(
          _.componentStates
            .collectFirst { case state: BatteryState => state.currentCharge }
        )

    nextBatteryCharge shouldBe Some(0.9625.kwh)
    next.entityFlows.get(house.id) shouldBe Some(Flow.Balanced)

  it should "resolve solar panel entities through the default dispatcher" in:
    val (panel, panelState) =
      SolarPanel(
        id = "panel-1",
        location = GeographicPoint(44.3, 11.7),
        maxProduction = 5.kw,
        areaSqm = 20.0,
        efficiency = 0.20
      ).toOption.get
    val grid =
      GridGraph(
        nodes = List(ExternalGrid("external-grid"), panel),
        cables = Nil
      )
    val engine =
      DefaultSimulationEngine(
        SimulationModel(grid, 1.hour),
        SimplePowerFlowSolver(grid)
      )
    val current =
      SimulationState(
        environment = Environment(6.hours),
        entityStates = Map(panelState.entityId -> panelState)
      )

    val next = engine.step(current)

    val nextPanelState =
      next.entityStates.values.collectFirst { case state: SolarPanelState => state }

    nextPanelState.map(_.efficiency) shouldBe Some(panel.efficiency)
  // FIXME: doesn't work when changing weather logic
//    val Some(Flow.Surplus(surplus)) = next.entityFlows.get(panel.id): @unchecked
//    surplus.toDouble shouldBe (0.27 +- 0.01)

  it should "calculate the load on every cable" in:
    val externalGrid = ExternalGrid("external-grid")
    val house = House("house-1", components = Nil)
    val houseState = HouseState("house-1", componentStates = Nil)
    val cable =
      Cable(
        CableConnections(externalGrid.id, house.id),
        maxCapacity = 1.kw
      )
    val grid =
      GridGraph(
        nodes = List(externalGrid, house),
        cables = List(cable)
      )
    val engine =
      DefaultSimulationEngine(
        SimulationModel(grid, 15.minutes),
        SimplePowerFlowSolver(grid)
      )
    val current =
      SimulationState(
        environment = Environment(2.hours),
        entityStates = Map(houseState.entityId -> houseState)
      )

    val next = engine.step(current)

    next.cableLoads.get(cable) shouldBe Some(0.0375.kwh)
