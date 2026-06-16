package org.gridsim.core.behaviour.house

import cats.data.State
import cats.Traverse
import cats.implicits.*
import org.gridsim.core.behaviour.{EnergyExchanger, EnergyResolver}
import org.gridsim.core.common.*
import org.gridsim.core.behaviour.EnergyExchanger.*
import org.gridsim.core.behaviour.shaping.DemandShaper
import org.gridsim.core.common.StochasticGenerator
import org.gridsim.core.model.*
import org.gridsim.core.model.house.House

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
  ): EnergyResolver[House[F]] with
    def resolve(h: House[F], env: Environment)(using delta: FiniteDuration): (House[F], Flow[Energy]) =
      val internalFlow = resolver.resolve(env.time.hour, h.strategy)

      /*val (afterProducersResidue, updatedProducers) = h.producers.traverse { prod =>
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
      )*/

      (h, internalFlow)
