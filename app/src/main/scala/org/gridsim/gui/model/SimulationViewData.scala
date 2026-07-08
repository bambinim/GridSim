package org.gridsim.gui.model

import org.gridsim.core.model.GridEntity
import org.gridsim.core.model.network.Cable
import org.gridsim.core.simulation.SimulationControllerState

/**
 * Represents the direction and status of energy flow for a node or system.
 */
enum FlowDirection:
  /** Consuming more energy than producing. */
  case Importing
  /** Producing more energy than consuming. */
  case Exporting
  /** Net energy flow is balanced (near zero). */
  case Balanced

/**
 * Represents the interactive selection in the graphical simulation viewport.
 */
enum Selection:
  /** No element is selected. */
  case NoSelection
  /** A node containing a grid entity is selected. */
  case SelectedNode(entity: GridEntity)
  /** A transmission cable is selected. */
  case SelectedCable(cable: Cable)

/**
 * UI-bound tree-like structure representation of a selected entity and its nested parts.
 *
 * @param id unique ID of the entity
 * @param title descriptive title suitable for card headers
 * @param fields flat list of key-value details for this entity level
 * @param components nested sub-components (such as installed batteries or panels)
 */
final case class DetailsEntity(
  id: String,
  title: String,
  fields: Seq[DetailField],
  components: Seq[DetailsEntity]
)

/**
 * A key-value pair detailing a specific attribute of a selection.
 *
 * @param field label of the attribute (e.g. "Max Capacity")
 * @param value formatted value of the attribute (e.g. "10 kWh")
 */
final case class DetailField(
  field: String,
  value: String
)

/**
 * Aggregate summary metrics of the current simulation run ready for GUI binding.
 *
 * @param controllerState the operational state of the runner (RUNNING, PAUSED, etc.)
 * @param simulatedMinutes total simulation minutes elapsed
 * @param hourOfDay current time of day in the simulation environment
 * @param netFlowKwh numerical value of net energy flow
 * @param netFlowKind category classification of flow (Importing, Exporting, Balanced)
 * @param entityCount total active grid nodes
 * @param cableCount total transmission lines
 */
final case class SummaryViewState(
  controllerState: SimulationControllerState,
  simulatedMinutes: Long,
  hourOfDay: Int,
  netFlowKwh: Double,
  netFlowKind: FlowDirection,
  entityCount: Int,
  cableCount: Int
)

