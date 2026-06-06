package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.{Energy, Flow, Power}
import org.gridsim.core.model.house.Occupancy.*
import org.gridsim.core.model.house.Size.*
import org.gridsim.core.model.house.{Occupancy, Size}

import scala.concurrent.duration.FiniteDuration

object ConsumptionProfile:

  def calculateConsume(size: Size, occupancy: Occupancy, hour: Int)(using delta: FiniteDuration): Flow[Energy] =
    val sizeMultiplier = size.multiplier

    val occupancyDemandRate = occupancy match
      case Vacant => 1.0
      case Traditional =>
        if (hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 22) then 8.0 else 2.0
      case SmartWorker =>
        if hour >= 8 && hour <= 23 then 5.0 else 2.0

    val demandPower = Power(occupancyDemandRate) * sizeMultiplier
    val amount = demandPower.toEnergy

    if amount > Energy.Zero then Flow.Deficit(amount) else Flow.Balanced
