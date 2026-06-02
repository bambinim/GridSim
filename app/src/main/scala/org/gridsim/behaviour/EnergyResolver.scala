package org.gridsim.behaviour

import org.gridsim.common.Units.Energy
import org.gridsim.model.{BaseHouse, Environment, GridEntity}

trait EnergyResolver[T]:
  def solve(node: T, env: Environment): Energy

object EnergyResolver:

  given EnergyResolver[BaseHouse] with
    def solve(house: BaseHouse, env: Environment): Energy =
      val balance = ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)
      balance

