package org.gridsim.gui.ports

import org.gridsim.core.simulation.SimulationModel
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.common.Energy.toFlow
import org.gridsim.core.common.kwh
import org.gridsim.core.model.Environment

final case class ExtractedSummary(
  simulatedMinutes: Long,
  hourOfDay: Int,
  netFlowKwh: Double,
  netFlowKind: Flow[Energy],
  entityCount: Int,
  cableCount: Int
)

class SummaryExtractor:
  def extract(
    model: SimulationModel, 
    entityFlows: Map[String, Flow[Energy]], 
    env: Environment
  ): ExtractedSummary =
    val netFlowKwh = entityFlows.values.map(_.value).sum
    ExtractedSummary(
      simulatedMinutes = env.time.toMinutes,
      hourOfDay = env.hourOfDay,
      netFlowKwh = netFlowKwh,
      netFlowKind = netFlowKwh.kwh.toFlow,
      entityCount = model.grid.nodes.size,
      cableCount = model.grid.cables.size
    )
