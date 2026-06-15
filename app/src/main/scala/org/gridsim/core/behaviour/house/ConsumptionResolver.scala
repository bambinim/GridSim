package org.gridsim.core.behaviour.house

import org.gridsim.core.behaviour.shaping.DemandShaper
import org.gridsim.core.common.StochasticGenerator
import org.gridsim.core.common.Units.{Energy, Flow, kw}
import org.gridsim.core.model.house.Occupancy.*
import org.gridsim.core.model.house.Size.*
import org.gridsim.core.model.house.{Occupancy, Size}

import scala.concurrent.duration.FiniteDuration

/**
 * Defines the contract for resolving the energy consumption of a house.
 *
 * A resolver interprets a [[ConsumptionStrategy]] to produce a concrete
 * energy flow for a given point in time, typically incorporating
 * stochasticity or physical constraints.
 */
trait ConsumptionResolver:
  /**
   * Calculates the energy demand for a specific hour and strategy.
   *
   * @param hour     The current hour of the day (0-23).
   * @param strategy The profile-specific strategy to use.
   * @param delta    The simulation tick duration.
   * @param shaper   The shaper used to apply randomness or smoothing to the demand.
   * @return A [[Flow.Deficit]] or [[Flow.Balanced]] based on the calculated demand.
   */
  def resolve(hour: Int, strategy: ConsumptionStrategy)(using
    delta: FiniteDuration,
    shaper: DemandShaper
  ): Flow[Energy]

object ConsumptionResolver:
  /** Default given instance using a stochastic model. */
  given ConsumptionResolver = new StochasticConsumptionResolver()
    
