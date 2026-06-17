package org.gridsim.core.behaviour.house

import cats.data.State
import cats.Traverse
import cats.Show.Shown.mat
import cats.implicits.*
import org.gridsim.core.behaviour.{EnergyExchanger, EnergyResolver}
import org.gridsim.core.common.*
import org.gridsim.core.behaviour.EnergyExchanger.*
import org.gridsim.core.behaviour.shaping.DemandShaper
import org.gridsim.core.common.StochasticGenerator
import org.gridsim.core.model.*
import org.gridsim.core.model.battery.{Battery, BatteryState}
import org.gridsim.core.model.house.{House, HouseState}

import scala.concurrent.duration.FiniteDuration

import scala.concurrent.duration.FiniteDuration

/**
 * Logic implementation for the [[House]] entity.
 *
 * Orchestrates the energy resolution process by:
 * 1. Calculating internal consumption using a [[ConsumptionResolver]].
 * 2. Processing internal Producers to cover demand or generate surplus.
 * 3. Processing internal Storages to store excess or cover remaining deficit.
 */
object HouseLogic:

  /**
   * Provides the [[EnergyResolver]] implementation for the [[House]].
   *
   * It requires a [[ConsumptionResolver]] and a [[DemandShaper]] to be
   * present in the implicit scope to handle stochasticity.
   */
  given houseResolver[F[_]: Traverse](using
    resolver: ConsumptionResolver,
    shaper: DemandShaper
  ): EnergyResolver[HouseState[F], House[F]] with
    def resolve(state: HouseState[F], h: House[F], env: Environment)(using delta: FiniteDuration): (HouseState[F], Flow[Energy]) =
      val internalFlow = resolver.resolve(env.time.hour, h.strategy)

      val simulation = for {
        updatedProducers <- state.producers.traverse { prod =>
          State[Flow[Energy], Producer] { currentFlow =>
            val (newState, nextResidue) = prod.state.exchange(prod, currentFlow, env)
            (nextResidue, updateComponent(prod, newState))
          }
        }

        updatedStorages <- state.storages.traverse { stor =>
          State[Flow[Energy], Storage] { currentFlow =>
            val (newState, nextResidue) = stor.state.exchange(stor, currentFlow, env)
            (nextResidue, updateComponent(stor, newState))
          }
        }
      }yield HouseState(updatedProducers, updatedStorages)

      val (finalResidue, newHouseState) = simulation.run(internalFlow).value

      (newHouseState, finalResidue)

  private def updateComponent[A <: GridEntity, S](comp: A, newState: S): A = comp match
    case b: Battery =>
      b.copy(state = newState.asInstanceOf[BatteryState]).asInstanceOf[A]
    case other => throw new IllegalArgumentException(s"Component not managed: ${other.getClass}")
