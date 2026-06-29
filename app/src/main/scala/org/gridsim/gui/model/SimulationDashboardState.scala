package org.gridsim.gui.model

import org.gridsim.core.simulation.SimulationControllerState
import org.gridsim.gui.model.*

case class SimulationDashboardState(
  controllerState: SimulationControllerState,
  simulatedMinutes: Long,
  hourOfDay: Int,
  netFlowKwh: Double,
  netFlowKind: FlowDirection,
  nodes: Seq[GridNodeViewData],
  cables: Seq[CableViewData],
  selectedEntity: Option[EntityDetailsViewData] = None
)
