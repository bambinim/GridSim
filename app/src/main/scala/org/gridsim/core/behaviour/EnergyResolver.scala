package org.gridsim.core.behaviour

import cats.data.State
import org.gridsim.core.common.Units.*
import org.gridsim.core.model.*
import scala.concurrent.duration.*

trait EnergyResolver[T]:
  def solve(env: Environment): State[T, Flow[Energy]]

object EnergyResolver:
  given EnergyResolver[BaseHouse] with
    def solve(env: Environment): State[BaseHouse, Flow[Energy]] =
      State.inspect: house =>
        ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)(using env.delta)

  given EnergyResolver[HouseWithBattery] with
    def solve(env: Environment): State[HouseWithBattery, Flow[Energy]] =
      for {
        house <- State.get[HouseWithBattery]
        consume = ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)(using env.delta)
        (newBattery, residue) = BatteryBehaviour.update(consume)(using env.delta).run(house.battery).value
        _ <- State.set(house.copy(battery = newBattery))
      } yield residue

object EnergyResolverSyntax:
  extension [A](node: A)(using resolver: EnergyResolver[A])
    def solve(env: Environment): (A, Flow[Energy]) =
      resolver.solve(env).run(node).value
