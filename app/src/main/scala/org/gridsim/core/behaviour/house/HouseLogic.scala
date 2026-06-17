package org.gridsim.core.behaviour.house

import cats.data.State
import cats.Traverse
import cats.implicits.*
import org.gridsim.core.behaviour.EnergyResolver
import org.gridsim.core.common.Units.*
import org.gridsim.core.common.Units.Flow.Balanced
import org.gridsim.core.model.*
import org.gridsim.core.model.house.House
import org.gridsim.core.behaviour.EnergyResolver.*

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
    def solve(flow: Flow[Energy], env: Environment)(using delta: FiniteDuration): State[House[F], Flow[Energy]] =
      for {
        house <- State.get[House[F]]
        internalFlow = ConsumptionProfile.calculateConsume(house.size, house.occupancy, env.time.toHours)
        initialResidue = internalFlow + flow

        (afterProducersResidue, updatedProducers) = house.producers.traverse { prod =>
          State[Flow[Energy], Producer] { currentFlow =>
            val (newProd, nextResidue) = prod.runSolve(currentFlow, env)
            (nextResidue, newProd)
          }
        }.run(initialResidue).value

        (afterStoragesResidue, updatedStorages) = house.storages.traverse { stor =>
          State[Flow[Energy], Storage] { currentFlow =>
            val (newStor, nextResidue) = stor.runSolve(currentFlow, env)
            (nextResidue, newStor)
          }
        }.run(afterProducersResidue).value

        _ <- State.modify[House[F]](_.copy(producers = updatedProducers, storages = updatedStorages))
      } yield afterStoragesResidue
