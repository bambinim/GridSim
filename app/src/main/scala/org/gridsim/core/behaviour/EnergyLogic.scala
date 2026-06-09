package org.gridsim.core.behaviour

import cats.data.State
import org.gridsim.core.common.Units.{Energy, Flow}
import org.gridsim.core.model.Environment

/**
 * Defines the core logic for energy processing for a given entity type.
 *
 * @tparam T The type of the entity (e.g., Battery).
 */
trait EnergyLogic[T]:
  /**
   * Processes the incoming energy flow and returns a state transition.
   *
   * @param flow   The incoming energy flow (surplus or deficit).
   * @param env    The current environmental conditions.
   * @return A State transition that returns the residual energy flow.
   */
  def process(flow: Flow[Energy], env: Environment): State[T, Flow[Energy]]
