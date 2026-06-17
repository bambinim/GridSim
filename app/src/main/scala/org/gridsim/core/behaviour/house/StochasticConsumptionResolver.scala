package org.gridsim.core.behaviour.house

import org.gridsim.core.behaviour.shaping.DemandShaper
import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.gridsim.core.common.Power.*
import cats.syntax.all.*

import scala.concurrent.duration.FiniteDuration

/**
 * A stochastic implementation of [[ConsumptionResolver]].
 *
 * It delegates the application of noise/variability to a [[DemandShaper]],
 * ensuring that the calculated demand varies realistically across simulation ticks.
 */
class StochasticConsumptionResolver extends ConsumptionResolver:
  override def resolve(hour: Long, strategy: ConsumptionStrategy)(using delta: FiniteDuration, shaper: DemandShaper): Flow[Energy] =
    val band = strategy.getBand(hour)

    val totalPower = shaper.shape(band.meanPower.toDouble, band.variance).kw
    val amount = totalPower.toEnergy

    if amount > Energy.Zero then Flow.Deficit(amount) else Flow.Balanced
