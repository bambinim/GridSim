package org.gridsim.gui.controller

import org.gridsim.core.common.Energy.toFlow
import org.gridsim.core.common.{Energy, Flow, kwh}
import org.gridsim.core.observability.SimulationData
import org.gridsim.core.simulation.{SimulationControllerState, SimulationModel, SimulationState}
import org.gridsim.gui.model.{FlowDirection, SummaryViewState}

class SimulationSummaryController(model: SimulationModel):
  def onSnapshot(
    snapshot: SimulationData.SimulationSnapshot,
    controllerState: SimulationControllerState
  ): SummaryViewState =
    toSummary(
      environmentTimeMinutes = snapshot.environment.time.toMinutes,
      hourOfDay = snapshot.environment.hourOfDay,
      entityFlows = snapshot.entityFlows,
      controllerState = controllerState
    )

  def onState(
    state: SimulationState,
    controllerState: SimulationControllerState
  ): SummaryViewState =
    toSummary(
      environmentTimeMinutes = state.environment.time.toMinutes,
      hourOfDay = state.environment.hourOfDay,
      entityFlows = state.entityFlows,
      controllerState = controllerState
    )

  private def toSummary(
    environmentTimeMinutes: Long,
    hourOfDay: Int,
    entityFlows: Map[String, Flow[Energy]],
    controllerState: SimulationControllerState
  ): SummaryViewState =
    val netFlowKwh =
      entityFlows.values.map(_.value).sum

    SummaryViewState(
      controllerState = controllerState,
      simulatedMinutes = environmentTimeMinutes,
      hourOfDay = hourOfDay,
      netFlowKwh = netFlowKwh,
      netFlowKind = flowDirection(netFlowKwh.kwh.toFlow),
      entityCount = model.grid.nodes.size,
      cableCount = model.grid.cables.size
    )

  private def flowDirection(flow: Flow[Energy]): FlowDirection =
    flow match
      case Flow.Surplus(_) => FlowDirection.Exporting
      case Flow.Deficit(_) => FlowDirection.Importing
      case Flow.Balanced => FlowDirection.Balanced
