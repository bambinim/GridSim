package org.gridsim.statistics

import cats.kernel.Monoid
import org.gridsim.core.observability.SimulationData.SimulationSnapshot

object CablesOverloadSampler:
  def sample(snapshot: SimulationSnapshot): CablesOverloadStatistic =
    val overloaded = snapshot.cableLoads.count { case (cable, load) =>
      load.instantPower(snapshot.delta).toDouble > cable.maxCapacity.toDouble
    }
    CablesOverloadStatistic(1L, overloaded.toLong)

final case class CablesOverloadStatistic(samples: Long, overloadedCableSamples: Long)

object CablesOverloadStatistic:
  val empty: CablesOverloadStatistic = CablesOverloadStatistic(0L, 0L)

  given Monoid[CablesOverloadStatistic] with
    def empty: CablesOverloadStatistic = CablesOverloadStatistic.empty
    def combine(a: CablesOverloadStatistic, b: CablesOverloadStatistic): CablesOverloadStatistic =
      CablesOverloadStatistic(a.samples + b.samples, a.overloadedCableSamples + b.overloadedCableSamples)
