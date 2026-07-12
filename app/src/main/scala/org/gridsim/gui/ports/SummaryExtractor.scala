package org.gridsim.gui.ports

import org.gridsim.core.simulation.SimulationModel
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.common.Energy.toFlow
import org.gridsim.core.common.kwh
import org.gridsim.core.model.Environment

import java.time.LocalDateTime

final case class ExtractedSummary(
                                   entityCount: Int,
                                   cableCount: Int
)

class SummaryExtractor:
  def extract(
              model: SimulationModel
  ): ExtractedSummary =
    ExtractedSummary(
      entityCount = model.grid.nodes.size,
      cableCount = model.grid.cables.size
    )
