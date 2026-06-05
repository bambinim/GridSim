package org.gridsim.core.behaviour

import cats.data.State
import org.gridsim.core.common.Units.{Energy, Flow}
import org.gridsim.core.model.{Environment, HouseComponent}
import org.gridsim.core.model.HouseComponent.BatteryComponent


trait ComponentResolver[T]:
  def solve(residueEnergy: Flow[Energy], env: Environment): State[T, Flow[Energy]]

object ComponentResolver:
  given ComponentResolver[BatteryComponent] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[BatteryComponent, Flow[Energy]] =
      for {
        batteryComp <- State.get[BatteryComponent]
        (newBattery, residue) = BatteryBehaviour.update(residueEnergy)(using env.delta).run(batteryComp.battery).value
        _ <- State.set[BatteryComponent](BatteryComponent(battery = newBattery))
      } yield residue

  given ComponentResolver[HouseComponent] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[HouseComponent, Flow[Energy]] =
      State {
        case bc: BatteryComponent =>
          val (newBc, residue) = summon[ComponentResolver[BatteryComponent]].solve(residueEnergy, env).run(bc).value
          (newBc, residue)
      }

object ComponentResolverSyntax:
  extension [A](node: A)(using resolver: ComponentResolver[A])
    def solve(residueEnergy: Flow[Energy], env: Environment): (A, Flow[Energy]) =
      resolver.solve(residueEnergy, env).run(node).value
