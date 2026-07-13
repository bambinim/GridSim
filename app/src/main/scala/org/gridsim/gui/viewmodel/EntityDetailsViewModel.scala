package org.gridsim.gui.viewmodel

import scalafx.beans.property.{ObjectProperty, ReadOnlyObjectProperty}
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.network.Cable
import org.gridsim.core.model.{Environment, GridEntity, GridEntityState}
import org.gridsim.core.simulation.SimulationModel
import org.gridsim.gui.model.{DetailsEntity, Selection}
import org.gridsim.gui.ports.{
  DetailDispatcher,
  ExtractedEntityDetails,
  ExtractedSelectionDetails
}
import org.gridsim.core.model.network.Cable
import scala.concurrent.duration.*

/** ViewModel for displaying and updating the details of the currently selected
  * entity.
  *
  * This ViewModel listens to changes in the current selection, updates details
  * based on incoming simulation snapshots, and maps core domain models/states
  * into UI-facing details representations (`DetailsEntity`).
  *
  * @param model
  *   the active simulation model containing the grid topology
  * @param selectionProp
  *   the property tracking the current selection in the UI
  */
class EntityDetailsViewModel(
    model: SimulationModel,
    selectionProp: ObjectProperty[Selection],
    deltaProvider: () => FiniteDuration
):
  private var lastEntityStates: Map[String, GridEntityState] = Map.empty
  private var lastEntityFlows: Map[String, Flow[Energy]] = Map.empty
  private var lastCableLoads: Map[Cable, Energy] = Map.empty
  private var lastEnvironment: Option[Environment] = None

  private val _detailsEntityProperty =
    ObjectProperty[DetailsEntity](emptyDetails)

  /** The read-only property exposing the details of the selected entity to the
    * view.
    */
  val detailsEntityProperty: ReadOnlyObjectProperty[DetailsEntity] =
    _detailsEntityProperty

  private def emptyDetails = DetailsEntity(
    id = "",
    title = "No selection",
    fields = Seq.empty,
    components = Seq.empty
  )

  selectionProp.onChange { (_, _, newSelection) =>
    lastEnvironment.foreach { env =>
      recalculate(
        newSelection,
        lastEntityStates,
        lastEntityFlows,
        lastCableLoads,
        env
      )
    }
  }

  /** Updates the cached entity states, flows, and environment, and triggers a
    * recalculation of the selected entity's details.
    *
    * @param entityStates
    *   a map of entity IDs to their current state in the simulation
    * @param entityFlows
    *   a map of entity IDs to their current energy flows
    * @param environment
    *   the current environmental state (e.g., solar radiation, temperature)
    * @param delta
    *   the current simulation step delta
    */
  def update(
      entityStates: Map[String, GridEntityState],
      entityFlows: Map[String, Flow[Energy]],
      cableLoads: Map[Cable, Energy],
      environment: Environment
  ): Unit =
    lastEntityStates = entityStates
    lastEntityFlows = entityFlows
    lastCableLoads = cableLoads
    lastEnvironment = Some(environment)

    recalculate(
      selectionProp.value,
      entityStates,
      entityFlows,
      cableLoads,
      environment
    )

  private def recalculate(
      selection: Selection,
      entityStates: Map[String, GridEntityState],
      entityFlows: Map[String, Flow[Energy]],
      cableLoads: Map[Cable, Energy],
      environment: Environment
  ): Unit =
    val extracted = DetailDispatcher.resolve(
      selection,
      entityStates,
      entityFlows,
      cableLoads,
      environment,
      deltaProvider()
    )
    _detailsEntityProperty.value = mapExtractedToView(extracted, environment)

  private def mapExtractedToView(
      extracted: ExtractedSelectionDetails,
      environment: Environment
  ): DetailsEntity =
    val componentViews = extracted.components.map { (comp, compState) =>
      val compExtracted =
        DetailDispatcher.resolveEntity(comp, compState, environment)
      mapEntityExtractedToView(comp, compExtracted, environment)
    }

    DetailsEntity(
      id = extracted.id,
      title = extracted.title,
      fields = extracted.fields,
      components = componentViews
    )

  private def mapEntityExtractedToView(
      entity: GridEntity,
      extracted: ExtractedEntityDetails,
      environment: Environment
  ): DetailsEntity =
    val componentViews = extracted.components.map { (comp, compState) =>
      val compExtracted =
        DetailDispatcher.resolveEntity(comp, compState, environment)
      mapEntityExtractedToView(comp, compExtracted, environment)
    }

    DetailsEntity(
      id = entity.id,
      title = s"${entity.getClass.getSimpleName}: ${entity.id}",
      fields = extracted.fields,
      components = componentViews
    )
