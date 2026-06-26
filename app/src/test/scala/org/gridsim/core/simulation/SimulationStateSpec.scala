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
      10.kw
    )

  private val entityFlows: Map[String, Flow[Energy]] = Map(
    "house-1" -> Deficit(2.kwh),
    "external-grid" -> Surplus(2.kwh)
  )

  private val cableLoads: Map[Cable, Energy] = Map(cable -> 2.kwh)

  "SimulationState" should "store a complete simulation snapshot" in:
    val state =
      SimulationState(
        environment = environment,
        entityStates = Map(houseState.entityId -> houseState),
        entityFlows = entityFlows,
        cableLoads = cableLoads
      )

    state.environment shouldBe environment
    state.entityStates shouldBe Map(houseState.entityId -> houseState)
    state.entityFlows shouldBe entityFlows
    state.cableLoads shouldBe cableLoads

  it should "store the external grid flow with the other entity flows" in:
    val state =
      SimulationState(
        environment = environment,
        entityStates = Map(houseState.entityId -> houseState),
        entityFlows = entityFlows,
        cableLoads = cableLoads
      )

    state.entityFlows.get("external-grid") shouldBe Some(Surplus(2.kwh))

  it should "support an initial snapshot without flows or cable loads" in:
    val initial =
      SimulationState(
        environment = Environment(0.hours),
        entityStates = Map(houseState.entityId -> houseState),
        entityFlows = Map.empty,
        cableLoads = Map.empty
      )
    
    initial.entityFlows shouldBe empty
    initial.cableLoads shouldBe empty

  it should "support structural equality" in:
    val first =
      SimulationState(environment, Map(houseState.entityId -> houseState), entityFlows, cableLoads)
    val second =
      SimulationState(environment, Map(houseState.entityId -> houseState), entityFlows, cableLoads)

    first shouldBe second

  it should "produce a new immutable snapshot through copy" in:
    val current =
      SimulationState(environment, Map(houseState.entityId -> houseState), entityFlows, cableLoads)
    val nextEnvironment = environment.advance(15.minutes)

    val next =
      current.copy(
        environment = nextEnvironment,
        entityFlows = Map("house-1" -> Balanced)
      )

    current.environment.time shouldBe 2.hours
    current.entityFlows shouldBe entityFlows

    next.environment.time shouldBe 2.hours + 15.minutes
    next.entityFlows shouldBe Map("house-1" -> Balanced)
    next.entityStates shouldBe current.entityStates
    next.cableLoads shouldBe current.cableLoads
