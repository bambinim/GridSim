package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.{Energy, Flow, Power}
import org.gridsim.core.model.house.Occupancy.*
import org.gridsim.core.model.house.Size.*
import org.gridsim.core.model.house.{Occupancy, Size}

import scala.concurrent.duration.FiniteDuration

object ConsumptionProfile:

  private val strategies: Map[Occupancy, ConsumptionStrategy] = Map(
    Occupancy.Traditional -> TraditionalStrategy,
    Occupancy.SmartWorker -> SmartWorkerStrategy,
    Occupancy.Vacant      -> VacantStrategy
  )

  def calculateConsume(size: Size, occupancy: Occupancy, hour: Int)(using delta: FiniteDuration): Flow[Energy] =
    val sizeMultiplier = size.multiplier
    val strategy = strategies.getOrElse(occupancy, VacantStrategy)

    val demandPower = strategy.demandAt(hour) * sizeMultiplier
    val amount = demandPower.toEnergy

    if amount > Energy.Zero then Flow.Deficit(amount) else Flow.Balanced
