package org.gridsim.gui.viewmodel

import scalafx.beans.property.{ObjectProperty, ReadOnlyObjectProperty}
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.{Environment, GridEntity, GridEntityState}
import org.gridsim.core.simulation.SimulationModel
import org.gridsim.gui.model.{DetailsEntity, Selection}
import org.gridsim.gui.ports.{DetailDispatcher, ExtractedEntityDetails, ExtractedSelectionDetails}

class EntityDetailsViewModel(
  model: SimulationModel,
  selectionProp: ObjectProperty[Selection]
):
  private var lastEntityStates: Map[String, GridEntityState] = Map.empty
  private var lastEntityFlows: Map[String, Flow[Energy]] = Map.empty
  private var lastEnvironment: Option[Environment] = None

  private val _detailsEntityProperty = ObjectProperty[DetailsEntity](emptyDetails)
  val detailsEntityProperty: ReadOnlyObjectProperty[DetailsEntity] = _detailsEntityProperty

  private def emptyDetails = DetailsEntity(
    id = "",
    title = "No selection",
    fields = Seq.empty,
    components = Seq.empty
  )

  selectionProp.onChange { (_, _, newSelection) =>
    lastEnvironment.foreach { env =>
      recalculate(newSelection, lastEntityStates, lastEntityFlows, env)
    }
  }

  def update(
    entityStates: Map[String, GridEntityState],
    entityFlows: Map[String, Flow[Energy]],
    environment: Environment
  ): Unit =
    lastEntityStates = entityStates
    lastEntityFlows = entityFlows
    lastEnvironment = Some(environment)
    recalculate(selectionProp.value, entityStates, entityFlows, environment)

  private def recalculate(
    selection: Selection,
    entityStates: Map[String, GridEntityState],
    entityFlows: Map[String, Flow[Energy]],
    environment: Environment
  ): Unit =
    val extracted = DetailDispatcher.resolve(selection, entityStates, entityFlows, environment)
    _detailsEntityProperty.value = mapExtractedToView(extracted, environment)

  private def mapExtractedToView(
    extracted: ExtractedSelectionDetails,
    environment: Environment
  ): DetailsEntity =
    val componentViews = extracted.components.map { (comp, compState) =>
      val compExtracted = DetailDispatcher.resolveEntity(comp, compState, environment)
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
      val compExtracted = DetailDispatcher.resolveEntity(comp, compState, environment)
      mapEntityExtractedToView(comp, compExtracted, environment)
    }

    DetailsEntity(
      id = entity.id,
      title = s"${entity.getClass.getSimpleName}: ${entity.id}",
      fields = extracted.fields,
      components = componentViews
    )
