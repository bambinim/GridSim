package org.gridsim.gui.ports

import DetailExtractor.given
import cats.implicits.toShow
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.storage.StorageState.percentage
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.model.{Environment, GridEntity, GridEntityState, SolarPanel, SolarPanelState}
import org.gridsim.gui.model.DetailField

trait DetailExtractor[E <: GridEntity, S <: GridEntityState]:
  def extract(entity: E, state: S, env: Environment): Seq[DetailField]

object DetailExtractor:
  given DetailExtractor[Battery, BatteryState] with
    def extract(entity: Battery, state: BatteryState, env: Environment): Seq[DetailField] =
      val percentageVal = entity.percentage(state)
      Seq(
        DetailField("Max Capacity", entity.maxCapacity.show),
        DetailField("State of Charge", f"$percentageVal%.1f%%"),
        DetailField("Current Charge", state.currentCharge.show),
        DetailField("Minimum State of Charge Threshold", f"${entity.minSoC}"),
        DetailField("Max Power Charge", entity.maxPowerCharge.show),
        DetailField("Max Power Discharge", entity.maxPowerDischarge.show)
      )

  given DetailExtractor[SolarPanel, SolarPanelState] with
    def extract(entity: SolarPanel, state: SolarPanelState, env: Environment): Seq[DetailField] =
      val weather = env.weather(entity.location)
      Seq(
        DetailField("Panel Area", f"${entity.areaSqm}%.1f m²"),
        DetailField("Max Efficiency", f"${entity.efficiency * 100}%.1f %%"),
        DetailField("Max Production", entity.maxProduction.show),
        DetailField("Irradiance in Panel", weather.irradiance.show),
        DetailField("Temperature in location", weather.temperature.toCelsius.show)
      )

  given DetailExtractor[House, HouseState] with
    def extract(entity: House, state: HouseState, env: Environment): Seq[DetailField] =
      Seq(
        DetailField("Connected Components", entity.components.size.toString)
      )

object DetailDispatcher:
  import DetailExtractor.given

  def resolve(entity: GridEntity, state: GridEntityState, env: Environment): Seq[DetailField] =
    (entity, state) match
      case (b: Battery, s: BatteryState) =>
        summon[DetailExtractor[Battery, BatteryState]].extract(b, s, env)
      case (p: SolarPanel, s: SolarPanelState) =>
        summon[DetailExtractor[SolarPanel, SolarPanelState]].extract(p, s, env)
      case (h: House, s: HouseState) =>
        summon[DetailExtractor[House, HouseState]].extract(h, s, env)
      case _ =>
        Seq(
          DetailField("Info", "No field available")
        )

