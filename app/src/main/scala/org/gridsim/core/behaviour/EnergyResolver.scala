package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.{Energy, Flow}
import org.gridsim.core.model.{BaseHouse, Environment, HouseWithBattery}

import scala.concurrent.duration.FiniteDuration


trait EnergyResolver[T]:
  def solve(node: T, env: Environment): (T, Flow[Energy])

object EnergyResolver:
  given EnergyResolver[BaseHouse] with
    def solve(house: BaseHouse, env: Environment): (BaseHouse, Flow[Energy]) =
      given FiniteDuration = env.delta
      (house, ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour))

  given EnergyResolver[HouseWithBattery] with
    def solve(house: HouseWithBattery, env: Environment): (HouseWithBattery, Flow[Energy]) =
      given FiniteDuration = env.delta
      val consumeFlow = ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)
      
      val (newBattery, residueFlow) = BatteryBehaviour.update(battery = house.battery, requested = consumeFlow)

      (house.copy(battery = newBattery), residueFlow)

object EnergyResolverSyntax:
  extension [A](node: A)(using resolver: EnergyResolver[A])
    def solve(env: Environment): (A, Flow[Energy]) =
      resolver.solve(node, env)
