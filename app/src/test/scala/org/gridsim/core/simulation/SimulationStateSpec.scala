package org.gridsim.core.simulation

import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.gridsim.core.model.Environment
import org.gridsim.core.model.house.HouseState
import org.gridsim.core.model.network.{Cable, CableConnections}
import org.gridsim.core.model.storage.battery.BatteryState
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class SimulationStateSpec extends AnyFlatSpec with Matchers:

  private val environment = Environment(2.hours)
  private val batteryState = BatteryState("battery-1", 5.kwh)
  private val houseState =
    HouseState("house-1", List(batteryState))
  private val cable =
    Cable(
      CableConnections("external-grid", "house-1"),
      10.kwh
    )

  private val entityFlows: Map[String, Flow[Energy]] = Map(
    "house-1" -> Deficit(2.kwh),
    "external-grid" -> Surplus(2.kwh)
  )

  private val cableLoads: Map[Cable, Energy] = Map(cable -> 2.kwh)

  "SimulationState" should "store a complete simulation snapshot" in:
    val state =
      SimulationState(
        tick = 3,
        environment = environment,
        entityStates = List(houseState),
        entityFlows = entityFlows,
        cableLoads = cableLoads
      )

    state.tick shouldBe 3
    state.environment shouldBe environment
    state.entityStates should contain only houseState
    state.entityFlows shouldBe entityFlows
    state.cableLoads shouldBe cableLoads

  it should "store the external grid flow with the other entity flows" in:
    val state =
      SimulationState(
        tick = 3,
        environment = environment,
        entityStates = List(houseState),
        entityFlows = entityFlows,
        cableLoads = cableLoads
      )

    state.entityFlows.get("external-grid") shouldBe Some(Surplus(2.kwh))

  it should "support an initial snapshot without flows or cable loads" in:
    val initial =
      SimulationState(
        tick = 0,
        environment = Environment(0.hours),
        entityStates = List(houseState),
        entityFlows = Map.empty,
        cableLoads = Map.empty
      )

    initial.tick shouldBe 0
    initial.entityFlows shouldBe empty
    initial.cableLoads shouldBe empty

  it should "support structural equality" in:
    val first =
      SimulationState(3, environment, List(houseState), entityFlows, cableLoads)
    val second =
      SimulationState(3, environment, List(houseState), entityFlows, cableLoads)

    first shouldBe second

  it should "produce a new immutable snapshot through copy" in:
    val current =
      SimulationState(3, environment, List(houseState), entityFlows, cableLoads)
    val nextEnvironment = environment.advance(15.minutes)

    val next =
      current.copy(
        tick = current.tick + 1,
        environment = nextEnvironment,
        entityFlows = Map("house-1" -> Balanced)
      )

    current.tick shouldBe 3
    current.environment.time shouldBe 2.hours
    current.entityFlows shouldBe entityFlows

    next.tick shouldBe 4
    next.environment.time shouldBe 2.hours + 15.minutes
    next.entityFlows shouldBe Map("house-1" -> Balanced)
    next.entityStates shouldBe current.entityStates
    next.cableLoads shouldBe current.cableLoads
