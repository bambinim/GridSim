package org.gridsim.gui.ports

import org.gridsim.core.simulation.SimulationModel
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.common.Energy.toFlow
import org.gridsim.core.common.kwh
import org.gridsim.core.model.Environment

import java.time.LocalDateTime

/**
 * Data container for aggregate simulation metrics extracted from a simulation state snapshot.
 *
 * @param simulatedMinutes total simulation runtime in minutes
 * @param dateTime current hour of the day (0-23)
 * @param netFlowKwh the total net energy flow (surplus or deficit) in kWh
 * @param netFlowKind the direction of the net energy flow (Surplus, Deficit, or Balanced)
 * @param entityCount the number of nodes in the grid topology
 * @param cableCount the number of cables in the grid topology
 */
final case class ExtractedSummary(
                                   simulatedMinutes: Long,
                                   dateTime: LocalDateTime,
                                   netFlowKwh: Double,
                                   netFlowKind: Flow[Energy],
                                   entityCount: Int,
                                   cableCount: Int
)

/**
 * Service port/adapter class that extracts and aggregates high-level metrics
 * from the simulation model and current environment state.
 */
class SummaryExtractor:
  /**
   * Aggregates and formats summary metrics for the given simulation snapshot.
   *
   * @param model the active simulation model containing the grid structure
   * @param entityFlows the active energy flows of all grid entities
   * @param env the current environmental factors
   * @return aggregated metric values wrapped in [[ExtractedSummary]]
   */
  def extract(
    model: SimulationModel, 
    entityFlows: Map[String, Flow[Energy]], 
    env: Environment
  ): ExtractedSummary =
    val netFlowKwh = entityFlows.values.map(_.value).sum
    ExtractedSummary(
      simulatedMinutes = env.time.toMinutes,
      dateTime = env.currentDateTime,
      netFlowKwh = netFlowKwh,
      netFlowKind = netFlowKwh.kwh.toFlow,
      entityCount = model.grid.nodes.size,
      cableCount = model.grid.cables.size
    )
