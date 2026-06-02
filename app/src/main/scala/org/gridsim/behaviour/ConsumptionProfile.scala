package org.gridsim.behaviour

import org.gridsim.common.Units.Energy
import org.gridsim.model.Occupancy.*
import org.gridsim.model.Size.*
import org.gridsim.model.{Occupancy, Size}

import scala.concurrent.duration.FiniteDuration

object ConsumptionProfile:

  def calculateConsume(size: Size, occupancy: Occupancy, hour: Int): Energy =
    val sizeMultiplier = size match
      case Small => 1.0
      case Medium => 1.5
      case Large => 2.0
      
    val occupancyDemand = occupancy match
      case Vacant => 1.0
      case Traditional =>
        if (hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 22) then 8.0 else 2.0
      case SmartWorker =>
        if hour >= 8 && hour <= 23 then 5.0 else 2.0
        
    Energy(occupancyDemand) * sizeMultiplier


