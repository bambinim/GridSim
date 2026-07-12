package org.gridsim.dsl.scenarios

import org.gridsim.core.behaviour.house.DefaultConsumptionStrategy.{
  commercialProfile,
  ecoProfile,
  traditionalProfile
}
import org.gridsim.core.common.{kw, kwh}
import org.gridsim.dsl.grid.Topology.*
import org.gridsim.dsl.grid.entities.BatteryBuilder.battery
import org.gridsim.dsl.grid.entities.HouseBuilder.{
  consumptionStrategy,
  contains,
  energyStorageSystems,
  id
}
import org.gridsim.dsl.grid.entities.SolarArrayBuilder.solarArray
import org.gridsim.dsl.simulation.SimulationBuilder
import org.gridsim.dsl.simulation.SimulationBuilder.{
  E,
  EG,
  entities,
  house,
  simulation,
  solarPowerPlant,
  tick,
  topology
}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

case class GridScenarioPreset(
    id: String,
    name: String,
    build: FiniteDuration => SimulationBuilder
)

object GridScenarioCatalog:

  val all: List[GridScenarioPreset] = List(
    GridScenarioPreset(
      id = "base-neighborhood",
      name = "Base neighborhood",
      build = baseNeighborhood
    ),
    GridScenarioPreset(
      id = "advanced-neighborhood",
      name = "Advanced neighborhood",
      build = advancedNeighborhood
    ),
    GridScenarioPreset(
      id = "solar-farm-grid",
      name = "Solar Farm Grid",
      build = solarFarmGrid
    ),
    GridScenarioPreset(
      id = "incoherent-neighborhood",
      name = "Incoherent Neighborhood (Error Testing)",
      build = incoherentNeighborhood
    )
  )

  def byId(id: String): Option[GridScenarioPreset] =
    all.find(_.id == id)

  private def baseNeighborhood(tickDuration: FiniteDuration = 15.minutes): SimulationBuilder =
    simulation {
      tick(tickDuration)

      entities {
        house:
          id("base-house-1")
          consumptionStrategy(traditionalProfile)

        house:
          id("base-house-2")
          consumptionStrategy(traditionalProfile)
          contains(
            solarArray id "base-house-2-pv" installedPower 4.kw location (
              44.49,
              11.34
            ) surface 22.0 efficiency 0.19
          )
          energyStorageSystems(
            battery id "base-house-2-battery" capacity 10.kwh maxChargingPower 3.kw maxDischargingPower 3.kw minSoC 0.15
          )

        house:
          id("base-house-3")
          consumptionStrategy(traditionalProfile)
      }

      topology {
        EG <-- 12.kw --> E(0)
        E(0) <-- 8.kw --> E(1)
        E(1) <-- 8.kw --> E(2)
      }
    }

  private def advancedNeighborhood(tickDuration: FiniteDuration = 15.minutes): SimulationBuilder =
    simulation {
      tick(tickDuration)

      entities {
        house:
          id("advanced-house-1")
          consumptionStrategy(traditionalProfile)

        house:
          id("advanced-house-2")
          consumptionStrategy(traditionalProfile)
          contains(
            solarArray id "advanced-house-2-pv" installedPower 6.kw location (
              44.50,
              11.34
            ) surface 32.0 efficiency 0.20
          )
          energyStorageSystems(
            battery id "advanced-house-2-battery" capacity 16.kwh maxChargingPower 4.kw maxDischargingPower 4.kw minSoC 0.15
          )

        house:
          id("advanced-house-3")
          consumptionStrategy(traditionalProfile)
          contains(
            solarArray id "advanced-house-3-pv" installedPower 3.kw location (
              44.51,
              11.35
            ) surface 18.0 efficiency 0.18
          )

        house:
          id("advanced-house-4")
          consumptionStrategy(traditionalProfile)
          energyStorageSystems(
            battery id "advanced-house-4-battery" capacity 8.kwh maxChargingPower 2.kw maxDischargingPower 3.kw minSoC 0.20
          )

        house:
          id("advanced-house-5")
          consumptionStrategy(traditionalProfile)
          contains(
            solarArray id "advanced-house-5-pv" installedPower 8.kw location (
              44.52,
              11.36
            ) surface 40.0 efficiency 0.21
          )
          energyStorageSystems(
            battery id "advanced-house-5-battery" capacity 20.kwh maxChargingPower 5.kw maxDischargingPower 5.kw minSoC 0.10
          )
      }

      topology {
        EG <-- 25.kw --> E(0)
        E(0) <-- 12.kw --> E(1)
        E(0) <-- 10.kw --> E(2)
        E(2) <-- 8.kw --> E(3)
        E(2) <-- 10.kw --> E(4)
      }
    }

  private def solarFarmGrid(tickDuration: FiniteDuration = 15.minutes): SimulationBuilder =
    simulation {
      tick(tickDuration)

      entities {
        solarPowerPlant(
          solarArray id "solar-farm-1" installedPower 100.kw location (
            44.53,
            11.37
          ) surface 500.0 efficiency 0.22
        )

        house:
          id("house-1")
          consumptionStrategy(traditionalProfile)
          energyStorageSystems(
            battery id "house-1-battery" capacity 20.kwh maxChargingPower 5.kw maxDischargingPower 5.kw minSoC 0.10
          )

        house:
          id("house-2")
          consumptionStrategy(traditionalProfile)
          contains(
            solarArray id "house-2-pv" installedPower 5.kw location (
              44.54,
              11.38
            ) surface 25.0 efficiency 0.20
          )

        house:
          id("house-3")
          consumptionStrategy(traditionalProfile)

        house:
          id("house-4")
          consumptionStrategy(traditionalProfile)
          energyStorageSystems(
            battery id "house-4-battery" capacity 15.kwh maxChargingPower 4.kw maxDischargingPower 4.kw minSoC 0.15
          )
      }

      topology {
        EG <-- 150.kw --> E(0)
        E(0) <-- 40.kw --> E(1)
        E(1) <-- 30.kw --> E(2)
        E(0) <-- 35.kw --> E(3)
        E(3) <-- 20.kw --> E(4)
      }
    }

  private def incoherentNeighborhood(tickDuration: FiniteDuration = 15.minutes): SimulationBuilder =
    simulation {
      tick(tickDuration)

      entities {
        house:
          id("h1")
          consumptionStrategy(traditionalProfile)
          contains(
            solarArray id "incoherent-pv" installedPower 4.kw location (44.49, 11.34) surface -22.0 efficiency 0.19
          )
          energyStorageSystems(
            battery id "incoherent-battery" capacity -10.kwh maxChargingPower 3.kw maxDischargingPower 3.kw minSoC 0.15
          )

        house:
          id("incoherent-house-2")
          consumptionStrategy(traditionalProfile)
          energyStorageSystems(
            battery id "incoherent-battery-2" capacity 10.kwh maxChargingPower 13.kw maxDischargingPower 3.kw minSoC 1.5
          )
      }

      topology {
        EG <-- 12.kw --> E(0)
        E(0) <-- 8.kw --> E(1)
      }
    }
