package org.gridsim.gui.model

import org.gridsim.core.common.*
import org.gridsim.core.common.Energy.toFlow
import org.gridsim.core.common.Flow.*
import org.gridsim.core.model.{GridEntity, SolarPanel}
import org.gridsim.core.model.house.House
import org.gridsim.core.model.network.{Cable, ExternalGrid}
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.simulation.{SimulationModel, SimulationSnapshot, SimulationState}

object SimulationDashboardMapper:

  def toDashboard(
    model: SimulationModel,
    snapshot: SimulationSnapshot,
    selectedEntityId: Option[String]
  ): SimulationDashboardState =
    val state = snapshot.state
    val netFlowKwh = state.entityFlows.values.map(_.value).sum

    SimulationDashboardState(
      controllerState = snapshot.controllerState,
      simulatedMinutes = state.environment.time.toMinutes,
      hourOfDay = state.environment.hourOfDay,
      netFlowKwh = netFlowKwh,
      netFlowKind = flowDirection(netFlowKwh.kwh.toFlow),
      nodes = model.grid.nodes.toSeq.map(nodeViewData(_, state)),
      cables = model.grid.cables.toSeq.map(cableViewData(_, state)),
      selectedEntity = selectedEntityId.flatMap(entityDetails(_, model, state))
    )

  private def nodeViewData(entity: GridEntity, state: SimulationState): GridNodeViewData =
    val flow = state.entityFlows.getOrElse(entity.id, Flow.Balanced)

    GridNodeViewData(
      id = entity.id,
      label = entity.id,
      kind = nodeKind(entity),
      flowDirection = flowDirection(flow),
      flowKwh = math.abs(flow.value)
    )

  private def cableViewData(cable: Cable, state: SimulationState): CableViewData =
    val load = state.cableLoads.getOrElse(cable, Energy.Zero)

    CableViewData(
      id = Seq(cable.connections.n1, cable.connections.n2).sorted.mkString("--"),
      fromId = cable.connections.n1,
      toId = cable.connections.n2,
      loadKwh = load.toDouble,
      capacityKw = cable.maxCapacity.toDouble
    )

  private def entityDetails(
    entityId: String,
    model: SimulationModel,
    state: SimulationState
  ): Option[EntityDetailsViewData] =
    model.grid.nodes.find(_.id == entityId).map { entity =>
      EntityDetailsViewData(
        id = entity.id,
        title = entity.id,
        kind = nodeKind(entity),
        metrics = entityMetrics(entity, state),
        children = childrenDetails(entity, state)
      )
    }

  private def childrenDetails(entity: GridEntity, state: SimulationState): Seq[EntityDetailsViewData] =
    entity match
      case house: House =>
        house.components.toSeq.map { component =>
          EntityDetailsViewData(
            id = component.id,
            title = component.id,
            kind = nodeKind(component),
            metrics = entityMetrics(component, state)
          )
        }

      case _ =>
        Seq.empty

  private def entityMetrics(entity: GridEntity, state: SimulationState): Seq[MetricViewData] =
    val flow = state.entityFlows.getOrElse(entity.id, Flow.Balanced)
    val baseMetrics = Seq(MetricViewData("Energy flow", f"${flow.value}%.2f kWh"))

    val specificMetrics = entity match
      case battery: Battery =>
        state.entityStates.get(battery.id).collect {
          case batteryState: BatteryState =>
            val stateOfCharge = batteryState.currentCharge / battery.maxCapacity

            Seq(
              MetricViewData("State of charge", f"${stateOfCharge * 100}%.1f%%"),
              MetricViewData("Current charge", f"${batteryState.currentCharge.toDouble}%.2f kWh"),
              MetricViewData("Capacity", f"${battery.maxCapacity.toDouble}%.2f kWh")
            )
        }.getOrElse(Seq.empty)

      case solarPanel: SolarPanel =>
        Seq(
          MetricViewData("Max production", f"${solarPanel.maxProduction.toDouble}%.2f kW"),
          MetricViewData("Efficiency", f"${solarPanel.efficiency * 100}%.1f%%")
        )

      case house: House =>
        Seq(MetricViewData("Components", house.components.size.toString))

      case _ =>
        Seq.empty

    baseMetrics ++ specificMetrics

  private def nodeKind(entity: GridEntity): NodeKind =
    entity match
      case _: House        => NodeKind.House
      case _: Battery      => NodeKind.Battery
      case _: SolarPanel   => NodeKind.SolarPanel
      case _: ExternalGrid => NodeKind.ExternalGrid
      case _               => NodeKind.Unknown

  private def flowDirection(flow: Flow[Energy]): FlowDirection =
    flow match
      case Flow.Surplus(_) => FlowDirection.Exporting
      case Flow.Deficit(_) => FlowDirection.Importing
      case Flow.Balanced   => FlowDirection.Balanced
