package org.gridsim.gui.model

import org.gridsim.core.simulation.SimulationControllerState

enum FlowDirection:
  case Importing, Exporting, Balanced

final case class SummaryViewState(
  controllerState: SimulationControllerState,
  simulatedMinutes: Long,
  hourOfDay: Int,
  netFlowKwh: Double,
  netFlowKind: FlowDirection,
  entityCount: Int,
  cableCount: Int
)

