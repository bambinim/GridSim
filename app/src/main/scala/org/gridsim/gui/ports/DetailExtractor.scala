package org.gridsim.gui.ports

import DetailExtractor.given
import cats.implicits.toShow
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.storage.StorageState.percentage
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.model.{Environment, GridEntity, GridEntityState, SolarPanel, SolarPanelState}
import org.gridsim.gui.model.{DetailField, Selection}
import org.gridsim.gui.model.Selection.{NoSelection, SelectedCable, SelectedNode}

final case class ExtractedEntityDetails(
  fields: Seq[DetailField],
  components: Seq[(GridEntity, GridEntityState)] = Seq.empty
)

final case class ExtractedSelectionDetails(
  id: String,
  title: String,
  fields: Seq[DetailField],
  components: Seq[(GridEntity, GridEntityState)] = Seq.empty
)

trait DetailExtractor[E <: GridEntity, S <: GridEntityState]:
  def extract(entity: E, state: S, env: Environment): ExtractedEntityDetails

object DetailExtractor:
  given DetailExtractor[Battery, BatteryState] with
    def extract(entity: Battery, state: BatteryState, env: Environment): ExtractedEntityDetails =
      val percentageVal = entity.percentage(state)
      ExtractedEntityDetails(
        fields = Seq(
          DetailField("Max Capacity", entity.maxCapacity.show),
          DetailField("State of Charge", f"${percentageVal * 100}%.1f %%"),
          DetailField("Current Charge", state.currentCharge.show),
          DetailField("Minimum State of Charge Threshold", f"${entity.minSoC}"),
          DetailField("Max Power Charge", entity.maxPowerCharge.show),
          DetailField("Max Power Discharge", entity.maxPowerDischarge.show)
        )
      )

  given DetailExtractor[SolarPanel, SolarPanelState] with
    def extract(entity: SolarPanel, state: SolarPanelState, env: Environment): ExtractedEntityDetails =
      val weather = env.weather(entity.location)
      ExtractedEntityDetails(
        fields = Seq(
          DetailField("Panel Area", f"${entity.areaSqm}%.1f m²"),
          DetailField("Max Efficiency", f"${entity.efficiency * 100}%.1f %%"),
          DetailField("Max Production", entity.maxProduction.show),
          DetailField("Irradiance in Panel", weather.irradiance.show),
          DetailField("Temperature in location", weather.temperature.toCelsius.show)
        )
      )

  given DetailExtractor[House, HouseState] with
    def extract(entity: House, state: HouseState, env: Environment): ExtractedEntityDetails =
      val componentPairs = entity.components.toSeq.flatMap { component =>
        state.componentStates
          .find(_.entityId == component.id)
          .map(compState => (component, compState))
      }
      ExtractedEntityDetails(
        fields = Seq(
          DetailField("Connected Components", entity.components.size.toString)
        ),
        components = componentPairs
      )

object DetailDispatcher:
  import DetailExtractor.given

  def resolve(
    selection: Selection,
    entityStates: Map[String, GridEntityState],
    entityFlows: Map[String, Flow[Energy]],
    environment: Environment
  ): ExtractedSelectionDetails =
    selection match
      case SelectedNode(entity) =>
        val entityState = entityStates.get(entity.id)
        val entityFlow = entityFlows.get(entity.id)

        val extracted = entityState match
          case Some(state) =>
            resolveEntity(entity, state, environment)
          case None =>
            ExtractedEntityDetails(fields = Seq.empty, components = Seq.empty)

        val dynamicDetails = entityFlow.toSeq.map(flow =>
          DetailField("Energy Balance", flow.show)
        )

        ExtractedSelectionDetails(
          id = entity.id,
          title = s"${entity.getClass.getSimpleName}: ${entity.id}",
          fields = extracted.fields ++ dynamicDetails,
          components = extracted.components
        )

      case SelectedCable(cable) =>
        val id = s"${cable.connections.n1} <-> ${cable.connections.n2}"
        ExtractedSelectionDetails(
          id = id,
          title = s"Cable: $id",
          fields = Seq(
            DetailField("Capacity", cable.maxCapacity.show)
          ),
          components = Seq.empty
        )

      case NoSelection =>
        ExtractedSelectionDetails(
          id = "",
          title = "No selection",
          fields = Seq.empty,
          components = Seq.empty
        )

  def resolveEntity(entity: GridEntity, state: GridEntityState, env: Environment): ExtractedEntityDetails =
    (entity, state) match
      case (b: Battery, s: BatteryState) =>
        summon[DetailExtractor[Battery, BatteryState]].extract(b, s, env)
      case (p: SolarPanel, s: SolarPanelState) =>
        summon[DetailExtractor[SolarPanel, SolarPanelState]].extract(p, s, env)
      case (h: House, s: HouseState) =>
        summon[DetailExtractor[House, HouseState]].extract(h, s, env)
      case _ =>
        ExtractedEntityDetails(
          fields = Seq(
            DetailField("Info", "No field available")
          )
        )
