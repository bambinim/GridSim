package org.gridsim.gui.model

import org.gridsim.core.model.GridEntity
import org.gridsim.core.model.network.Cable
import org.gridsim.core.simulation.SimulationControllerState

enum FlowDirection:
  case Importing, Exporting, Balanced

enum Selection:
  case NoSelection
  case SelectedNode(entity: GridEntity)
  case SelectedCable(cable: Cable)

final case class DetailsEntity(
  id: String,
  title: String,
  fields: Seq[DetailField],
  components: Seq[DetailsEntity]
)

final case class DetailField(
  field: String,
  value: String
)

final case class SummaryViewState(
  controllerState: SimulationControllerState,
  simulatedMinutes: Long,
  hourOfDay: Int,
  netFlowKwh: Double,
  netFlowKind: FlowDirection,
  entityCount: Int,
  cableCount: Int
)

