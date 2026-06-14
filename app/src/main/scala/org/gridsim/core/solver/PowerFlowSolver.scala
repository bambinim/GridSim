package org.gridsim.core.solver

import scala.collection.Map
import org.gridsim.core.model.network.{Cable, GridGraph}
import org.gridsim.core.common.Units.{Energy, Flow}

trait PowerFlowSolver:
  /**
   * Computes the energy load on each cable in the grid.
   *
   * Given the energy flow (surplus/deficit) of each entity node and the grid topology,
   * this method calculates how much energy flows through each cable. The returned
   * Energy value is always non-negative (absolute load), representing the magnitude
   * of the flow regardless of direction.
   *
   * @param entityFlowMap A map from entity ID to its net energy flow (surplus or deficit).
   * @param graph         The grid topology containing nodes and cables.
   * @return A map from each cable to the absolute energy load flowing through it.
   */
  def solve(entityFlowMap: Map[String, Flow[Energy]], graph: GridGraph): Map[Cable, Energy]

