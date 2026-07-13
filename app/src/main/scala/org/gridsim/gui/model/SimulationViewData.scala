package org.gridsim.gui.model

import org.gridsim.core.model.GridEntity
import org.gridsim.core.model.network.CableConnections

/** Represents the interactive selection in the graphical simulation viewport.
  */
enum Selection:
  /** No element is selected. */
  case NoSelection

  /** A node containing a grid entity is selected. */
  case SelectedNode(entity: GridEntity)

  /** A transmission cable is selected. */
  case SelectedCable(cable: CableConnections)

/** UI-bound tree-like structure representation of a selected entity and its
  * nested parts.
  *
  * @param id
  *   unique ID of the entity
  * @param title
  *   descriptive title suitable for card headers
  * @param fields
  *   flat list of key-value details for this entity level
  * @param components
  *   nested sub-components (such as installed batteries or panels)
  */
final case class DetailsEntity(
    id: String,
    title: String,
    fields: Seq[DetailItem],
    components: Seq[DetailsEntity]
)

/** A key-value pair detailing a specific attribute of a selection.
  *
  * @param field
  *   label of the attribute (e.g. "Max Capacity")
  * @param value
  *   formatted value of the attribute (e.g. "10 kWh")
  */
sealed trait DetailItem
final case class DetailField(field: String, value: String) extends DetailItem
case object DetailSeparator extends DetailItem
