package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.Energy
import org.gridsim.core.model.{BaseHouse, Environment}


trait EnergyResolver[T]:
  def solve(node: T, env: Environment): Energy

object EnergyResolver:
  given EnergyResolver[BaseHouse] with
    def solve(house: BaseHouse, env: Environment): Energy =
      ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)

object EnergyResolverSyntax:
  extension [A](node: A)(using resolver: EnergyResolver[A])
    def solve(env: Environment): Energy =
      resolver.solve(node, env)
