package org.gridsim.gui.ports

import DetailExtractor.given
import cats.implicits.toShow
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.common.Energy.toPower
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.model.network.Cable
import org.gridsim.core.model.storage.StorageState.percentage
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.model.{
  Environment,
  GridEntity,
  GridEntityState,
  SolarPanel,
  SolarPanelState
}
import org.gridsim.gui.model.{DetailField, DetailItem, DetailSeparator, Selection}
import org.gridsim.gui.model.Selection.{
  NoSelection,
  SelectedCable,
  SelectedNode
}
import org.gridsim.core.model.network.Cable
import scala.concurrent.duration.FiniteDuration

/** Extracted details for a specific grid entity.
  *
  * @param fields
  *   key-value detail pairs suitable for rendering
  * @param components
  *   nested/sub-components installed on this entity paired with their current
  *   state
  */
final case class ExtractedEntityDetails(
    fields: Seq[DetailItem],
    components: Seq[(GridEntity, GridEntityState)] = Seq.empty
)

/** Extracted details for the currently active UI selection.
  *
  * @param id
  *   unique identifier of the selection (e.g. node ID or cable endpoints)
  * @param title
  *   display title for the selection card
  * @param fields
  *   key-value detail pairs for the selection
  * @param components
  *   nested components within this selection
  */
final case class ExtractedSelectionDetails(
    id: String,
    title: String,
    fields: Seq[DetailItem],
    components: Seq[(GridEntity, GridEntityState)] = Seq.empty
)

/** Type class port for extracting display details from a specific domain entity
  * and state.
  *
  * @tparam E
  *   the specific subclass of GridEntity
  * @tparam S
  *   the corresponding state subclass of GridEntityState
  */
trait DetailExtractor[E <: GridEntity, S <: GridEntityState]:
  /** Extracts displayable fields and component information from the given
    * entity.
    *
    * @param entity
    *   the grid entity instance
    * @param state
    *   the current state of the entity
    * @param env
    *   the current environmental factors (weather, sunlight, etc.)
    * @return
    *   ExtractedEntityDetails containing display properties
    */
  def extract(entity: E, state: S, env: Environment): ExtractedEntityDetails

/** Contains standard implementations/instances of the DetailExtractor type
  * class.
  */
object DetailExtractor:
  given DetailExtractor[Battery, BatteryState] with
    def extract(
        entity: Battery,
        state: BatteryState,
        env: Environment
    ): ExtractedEntityDetails =
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
    def extract(
        entity: SolarPanel,
        state: SolarPanelState,
        env: Environment
    ): ExtractedEntityDetails =
      val weather = env.weather(entity.location)
      ExtractedEntityDetails(
        fields = Seq(
          DetailField("Panel Area", f"${entity.areaSqm}%.1f m²"),
          DetailField("Max Efficiency", f"${entity.efficiency * 100}%.1f %%"),
          DetailField("Max Production", entity.maxProduction.show),
          DetailField("Irradiance in Panel", weather.irradiance.show),
          DetailField(
            "Temperature in location",
            weather.temperature.toCelsius.show
          )
        )
      )

  given DetailExtractor[House, HouseState] with
    def extract(
        entity: House,
        state: HouseState,
        env: Environment
    ): ExtractedEntityDetails =
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

/** Utility dispatcher responsible for resolving details from a selected entity
  * or cable, pattern matching and summoning the appropriate
  * [[DetailExtractor]].
  */
object DetailDispatcher:
  import DetailExtractor.given

  /** Resolves the details of the active UI selection.
    *
    * @param selection
    *   the current selection model
    * @param entityStates
    *   the active states of all grid entities
    * @param entityFlows
    *   the active energy flows of all grid entities
    * @param environment
    *   the current simulation environment
    * @return
    *   resolved display details for the selection
    */
  def resolve(
      selection: Selection,
      entityStates: Map[String, GridEntityState],
      entityFlows: Map[String, Flow[Energy]],
      cableLoads: Map[Cable, Energy],
      environment: Environment,
      tickDelta: FiniteDuration
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

        val dynamicDetails =
          entityFlow.toSeq.map(flow => DetailField("Energy Balance", flow.show))

        ExtractedSelectionDetails(
          id = entity.id,
          title = s"${entity.getClass.getSimpleName}: ${entity.id}",
          fields = extracted.fields ++ dynamicDetails,
          components = extracted.components
        )

      case SelectedCable(cable) =>
        val id = s"${cable.n1} <-> ${cable.n2}"
        ExtractedSelectionDetails(
          id = id,
          title = s"Cable: $id",
          // fields = Seq(
          //   DetailField("Capacity", cable.maxCapacity.show)
          // ),
          fields = cableLoads
            .filter(_._1.connections == cable)
            .zipWithIndex
            .flatMap { case ((c, energy), idx) =>
              Seq(
                DetailSeparator,
                DetailField("Cable #", (idx + 1).toString),
                DetailField("Capacity", c.maxCapacity.show),
                DetailField(
                  "Average instant power load",
                  energy.instantPower(tickDelta).show
                )
              )
            }
            .drop(1)
            .toSeq,
          components = Seq.empty
        )

      case NoSelection =>
        ExtractedSelectionDetails(
          id = "",
          title = "No selection",
          fields = Seq.empty,
          components = Seq.empty
        )

  /** Resolves the details of a specific grid entity using its runtime state.
    *
    * @param entity
    *   the entity to query
    * @param state
    *   the current state of that entity
    * @param env
    *   the current simulation environment
    * @return
    *   resolved display details for the entity
    */
  def resolveEntity(
      entity: GridEntity,
      state: GridEntityState,
      env: Environment
  ): ExtractedEntityDetails =
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
