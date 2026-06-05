package org.gridsim.core.behaviour

import cats.data.State
import org.gridsim.core.common.Units.*
import org.gridsim.core.model.*
import org.gridsim.core.model.HouseComponent.BatteryComponent
import org.gridsim.core.behaviour.ComponentResolverSyntax.*

import scala.concurrent.duration.*

trait EnergyResolver[T]:
  def solve(env: Environment): State[T, Flow[Energy]]

object EnergyResolver:
  given EnergyResolver[House] with
    def solve(env: Environment): State[House, Flow[Energy]] =
      for {
        house <- State.get[House]
        consume = ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)(using env.delta)
        totalResidue <- house.components.zipWithIndex.foldLeft(State.pure[House, Flow[Energy]](consume)){
          case (accState, (comp, index)) =>
            for {
              currentFlow <- accState
              // Changed <- to = because solve returns a tuple, and to avoid withFilter error
              (newComponent, residue) = comp.solve(currentFlow, env)
              _ <- State.modify[House](h => h.copy(components = h.components.updated(index, newComponent)))
            } yield residue
        }
      } yield totalResidue


object EnergyResolverSyntax:
  extension [A](node: A)(using resolver: EnergyResolver[A])
    def solve(env: Environment): (A, Flow[Energy]) =
      resolver.solve(env).run(node).value
