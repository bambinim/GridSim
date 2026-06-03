package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.Energy
import org.gridsim.core.model.Occupancy.*
import org.gridsim.core.model.Size.*
import org.gridsim.core.model.{Occupancy, Size}

import scala.concurrent.duration.FiniteDuration

object ConsumptionProfile:

  def calculateConsume(size: Size, occupancy: Occupancy, hour: Int): Energy =
    val sizeMultiplier = size.multiplier

    val occupancyDemand = occupancy match
      case Vacant => 1.0
      case Traditional =>
        if (hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 22) then 8.0 else 2.0
      case SmartWorker =>
        if hour >= 8 && hour <= 23 then 5.0 else 2.0

    Energy(occupancyDemand) * sizeMultiplier


