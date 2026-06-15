package org.gridsim.core.behaviour.house

import cats.data.State
import cats.Traverse
import cats.implicits.*
import org.gridsim.core.behaviour.{EnergyResolver, EnergyExchanger}
import org.gridsim.core.behaviour.EnergyExchanger.*
import org.gridsim.core.common.Units.*
import org.gridsim.core.model.*
import org.gridsim.core.model.house.House

import scala.concurrent.duration.FiniteDuration

/**
 * Implementation of [[EnergyResolver]] for [[House]].
 * Orchestrates energy flows between consumption, producers, and storages.
 */
object HouseLogic:

  /**
   * Resolver instance for a [[House]].
   * Follows a multi-pass approach:
   * 1. Internal consumption is calculated.
   * 2. Producers are processed to cover demand or create surplus.
   * 3. Storages are processed to store surplus or cover remaining deficit.
   */
  given houseResolver[F[_]: Traverse]: EnergyResolver[House[F]] with
    def resolve(h: House[F], env: Environment)(using delta: FiniteDuration): (House[F], Flow[Energy]) = { // Aggiunte le graffe

      val internalFlow = ConsumptionProfile.calculateConsume(h.size, h.occupancy, env.hour)

      val (afterProducersResidue, updatedProducers) = h.producers.traverse { prod =>
        State[Flow[Energy], Producer] { currentFlow =>
          val (newProd, nextResidue) = prod.exchange(currentFlow, env)
          (nextResidue, newProd)
        }
      }.run(internalFlow).value

      val (afterStoragesResidue, updatedStorages) = h.storages.traverse { stor =>
        State[Flow[Energy], Storage] { currentFlow =>
          val (newStor, nextResidue) = stor.exchange(currentFlow, env)
          (nextResidue, newStor)
        }
      }.run(afterProducersResidue).value
      
      val updatedHouse = h.copy(
        producers = updatedProducers,
        storages = updatedStorages
      )

      (updatedHouse, afterStoragesResidue)
    }