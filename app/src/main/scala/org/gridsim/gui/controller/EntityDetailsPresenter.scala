package org.gridsim.gui.controller

import cats.syntax.all.toShow
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.{Environment, GridEntity, GridEntityState}
import org.gridsim.core.model.house.{House, HouseState}
import org.gridsim.core.simulation.SimulationModel
import org.gridsim.gui.model.Selection.{NoSelection, SelectedCable, SelectedNode}
import org.gridsim.gui.model.{DetailField, DetailsEntity, Selection}
import org.gridsim.gui.ports.DetailDispatcher

class EntityDetailsPresenter(model: SimulationModel):
  def mapStateToView(
    selection: Selection,
    entityStates: Map[String, GridEntityState],
    entityFlows: Map[String, Flow[Energy]],
    environment: Environment
  ): DetailsEntity =
    selection match
      case SelectedNode(entity) =>
        val entityState = entityStates.get(entity.id)
        val entityFlow = entityFlows.get(entity.id)

        val staticDetails =
          entityState.fold(Seq.empty)(state =>
            DetailDispatcher.resolve(entity, state, environment)
          )

        val dynamicDetails =
          entityFlow.toSeq.map(flow =>
            DetailField("Energy Balance", flow.show)
          )

        val componentDetails =
          (entity, entityState) match
            case (house: House, Some(s: HouseState)) =>
              house.components.toSeq.map { component =>
                mapComponentsToView(
                  component,
                  s.componentStates
                    .find(_.entityId == component.id)
                    .get,
                  environment
                )
              }

            case _ =>
              Seq.empty

        DetailsEntity(
          id = entity.id,
          title = s"${entity.getClass.getSimpleName}: ${entity.id}",
          fields = staticDetails ++ dynamicDetails,
          components = componentDetails
        )

      case SelectedCable(cable) =>
        val id = s"${cable.connections.n1} <-> ${cable.connections.n2}"
        DetailsEntity(
          id = id,
          title = s"Cable: $id",
          fields = Seq(
            DetailField("Capacity", cable.maxCapacity.show)
          ),
          components = Seq.empty
        )

      case NoSelection =>
        DetailsEntity(
          id = "",
          title = "No selection",
          fields = Seq.empty,
          components = Seq.empty
        )

  def mapComponentsToView(
    entity: GridEntity,
    entityState: GridEntityState,
    environment: Environment
  ): DetailsEntity =
    val details = DetailDispatcher.resolve(entity = entity, state = entityState, env = environment)

    DetailsEntity(
      id = entity.id,
      title = s"${entity.getClass.getSimpleName}: ${entity.id}",
      fields = details,
      components = Seq.empty
    )
