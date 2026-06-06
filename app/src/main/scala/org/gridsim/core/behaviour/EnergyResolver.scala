package org.gridsim.core.behaviour

import cats.data.State
import cats.implicits.*
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.Balanced
import org.gridsim.core.model.*
import org.gridsim.core.model.house.{House, HouseComponent}
import org.gridsim.core.model.battery.Battery

import scala.concurrent.duration.*

trait EnergyResolver[T]:
  def solve(flow: Flow[Energy], env: Environment): State[T, Flow[Energy]]

object EnergyResolver:
  given EnergyResolver[House] with
    def solve(flow: Flow[Energy], env: Environment): State[House, Flow[Energy]] =
      for {
        house <- State.get[House]
        // The house starts by calculating its own consumption/production balance
        internalResidue = ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.hour)(using env.delta)
        // Combine with incoming flow from grid
        initialResidue = internalResidue + flow

        // Traverse through components, threading the flow residue

        (totalResidue, updatedComponents) = house.components.traverse { comp =>
          State[Flow[Energy], HouseComponent] { currentFlow =>
            val (newComp, nextResidue) = summon[EnergyResolver[HouseComponent]].solve(currentFlow, env).run(comp).value
            (nextResidue, newComp)
          }
        }.run(initialResidue).value

        _ <- State.modify[House](_.copy(components = updatedComponents))
      } yield totalResidue

  given EnergyResolver[Battery] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[Battery, Flow[Energy]] =
      BatteryBehaviour.update(residueEnergy)(using env.delta)

  given EnergyResolver[HouseComponent] with
    def solve(residueEnergy: Flow[Energy], env: Environment): State[HouseComponent, Flow[Energy]] =
      State {
        case b: Battery =>
          val (newB, residue) = summon[EnergyResolver[Battery]].solve(residueEnergy, env).run(b).value
          (newB, residue)
      }



object EnergyResolverSyntax:
  extension [A](node: A)(using resolver: EnergyResolver[A])
    def solve(flow: Flow[Energy], env: Environment): State[A, Flow[Energy]] =
      resolver.solve(flow, env)

    def runSolve(flow: Flow[Energy], env: Environment): (A, Flow[Energy]) =
      resolver.solve(flow, env).run(node).value

    def solve(env: Environment): State[A, Flow[Energy]] =
      resolver.solve(Balanced, env)

    def runSolve(env: Environment): (A, Flow[Energy]) =
      resolver.solve(Balanced, env).run(node).value
