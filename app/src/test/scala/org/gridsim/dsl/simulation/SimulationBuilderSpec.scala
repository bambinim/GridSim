package org.gridsim.dsl.simulation

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.gridsim.dsl.simulation.SimulationBuilder.{
  simulation,
  entities,
  house,
  E,
  EG,
  topology
}
import org.gridsim.dsl.grid.Topology._
import org.gridsim.core.common.{kw, kwh}
import org.gridsim.dsl.grid.entities.HouseBuilder.consumptionStrategy
import org.gridsim.core.behaviour.house.DefaultConsumptionStrategy
import org.gridsim.core.behaviour.house.DefaultConsumptionStrategy.traditionalProfile
import org.gridsim.dsl.simulation.SimulationBuilder.tick
import cats.syntax.all.*
import scala.concurrent.duration.DurationInt
import org.gridsim.dsl.grid.entities.HouseBuilder
import org.gridsim.core.model.Environment
import org.gridsim.dsl.grid.entities.HouseBuilder.contains
import org.gridsim.dsl.grid.entities.SolarArrayBuilder.solarArray
import org.gridsim.dsl.grid.entities.HouseBuilder.energyStorageSystems
import org.gridsim.dsl.grid.entities.BatteryBuilder.battery

@RunWith(classOf[JUnitRunner])
class SimulationBuilderSpec extends AnyFlatSpec with Matchers:

  val builder = simulation {
    tick(5.seconds)
    entities {
      house:
        consumptionStrategy(traditionalProfile)
      house:
        consumptionStrategy(traditionalProfile)
        contains(
          solarArray installedPower 5.kw location (
            0.0,
            0.0
          ) surface 10 efficiency 0.98
        )
        energyStorageSystems(
          battery capacity 20.kwh maxChargingPower 2.kw maxDischargingPower 3.kw minSoC 0.15
        )
      house:
        consumptionStrategy(traditionalProfile)
    }
    topology {
      EG <-- 10.kw --> E(0)
      E(0) <-- 20.kw --> E(1)
      E(1) <-- 30.kw --> E(2)
    }
  }

  "A SimulationBuilder" should "be empty by default" in:
    val builder = simulation {}
    builder.entitiesBuilders shouldBe empty
    builder.topologyBlock shouldBe None
    builder.tickDelta shouldBe None

  "A fully-compiled simulation builder" should "contain all specified elements" in:
    builder.tickDelta shouldBe Some(5.seconds)
    builder.topologyBlock.isDefined shouldBe true
    builder.entitiesBuilders.length shouldBe 3

  it should "build a simulation containing all specified informations" in:
    val (model, state) = builder.build().getOrElse(fail())
    model.delta shouldBe 5.seconds
    model.grid.cables.size shouldBe 3
    model.grid.nodes.size shouldBe 4
    state.environment shouldBe Environment(0.seconds)
    state.entityStates.size shouldBe 3
