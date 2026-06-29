package org.gridsim.gui.model

enum NodeKind:
  case House, Battery, SolarPanel, ExternalGrid, Unknown

enum FlowDirection:
  case Importing, Exporting, Balanced

final case class GridNodeViewData(
  id: String,
  label: String,
  kind: NodeKind,
  flowDirection: FlowDirection,
  flowKwh: Double
)

final case class CableViewData(
  id: String,
  fromId: String,
  toId: String,
  loadKwh: Double,
  capacityKw: Double,
)

final case class MetricViewData(
 label: String,
 value: String,
)

final case class EntityDetailsViewData(
  id: String,
  title: String,
  kind: NodeKind,
  metrics: Seq[MetricViewData],
  children: Seq[EntityDetailsViewData] = Seq.empty
)

