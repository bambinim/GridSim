package org.gridsim.core.behaviour

import org.gridsim.core.common.Units.{Energy, Power}
import org.gridsim.core.model.{BaseHouse, Environment, HouseWithBattery}

import scala.concurrent.duration.DurationInt


trait EnergyResolver[T]:
  def solve(node: T, env: Environment): (T, Energy)

object EnergyResolver:
  given EnergyResolver[BaseHouse] with
    def solve(house: BaseHouse, env: Environment): (BaseHouse, Energy) =
      (house, ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour))

  given EnergyResolver[HouseWithBattery] with
    def solve(house: HouseWithBattery, env: Environment): (HouseWithBattery, Energy) =
      val requestedPower = Power(-ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour).toDouble)
      val (newBattery, energyExceed) = BatteryBehaviour.update(battery = house.battery, requestedPower = Power(requestedPower.toDouble), delta = 1.hour)

      (house.copy(battery = newBattery), energyExceed)

object EnergyResolverSyntax:
  extension [A](node: A)(using resolver: EnergyResolver[A])
    def solve(env: Environment): (A, Energy) =
      resolver.solve(node, env)
