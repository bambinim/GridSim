package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.{Energy, Flow, Power}
import org.gridsim.core.model.house.Occupancy.*
import org.gridsim.core.model.house.Size.*
import org.gridsim.core.model.house.{Occupancy, Size}

import scala.concurrent.duration.FiniteDuration

/**
 * Object used as entry point to calculate the consumption demand
 * of an [[House]] by mapping the [[Occupancy]] type and their respective
 * [[ConsumptionStrategy]]
 */
object ConsumptionProfile:

  private val strategies: Map[Occupancy, ConsumptionStrategy] = Map(
    Occupancy.Traditional -> TraditionalStrategy,
    Occupancy.SmartWorker -> SmartWorkerStrategy,
    Occupancy.Vacant      -> VacantStrategy
  )

  /**
   * Compute the energy demand for a [[House]] at a specific time.
   *
   * The calculation multiplies the base power demand (defined by the strategy)
   * by the house's size factor and converts it into energy based on the provided
   * simulation time step.
   *
   * @param size The [[Size]] of the [[House]] used as a scaling multiplier for demand.
   * @param occupancy The [[Occupancy]] profile determining the behaviour.
   * @param hour The current hour(0-23) of the day.
   * @param delta The duration of the simulation Tick.
   * @return A [[Flow.Deficit]] rapresenting the required energy
   */
  def calculateConsume(size: Size, occupancy: Occupancy, hour: Int)(using delta: FiniteDuration): Flow[Energy] =
    val sizeMultiplier = size.multiplier
    val strategy = strategies.getOrElse(occupancy, VacantStrategy)

    val demandPower = strategy.demandAt(hour) * sizeMultiplier
    val amount = demandPower.toEnergy

    if amount > Energy.Zero then Flow.Deficit(amount) else Flow.Balanced
