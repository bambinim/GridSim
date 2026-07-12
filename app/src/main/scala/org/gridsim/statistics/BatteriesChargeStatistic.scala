package org.gridsim.statistics

import org.gridsim.core.common.Energy.given
import cats.implicits.catsKernelOrderingForOrder
import cats.kernel.Monoid
import org.gridsim.core.common.{Energy, kwh}
import org.gridsim.core.model.house.HouseState
import org.gridsim.core.model.storage.battery.BatteryState
import org.gridsim.core.observability.SimulationData.EntityStatesData

object BatteriesChargeSampler:
  def sample(statesData: EntityStatesData): BatteriesChargeStatistic =
    val charges = statesData.states.values.collect[Energy] {
      case b: BatteryState => b.currentCharge
      case h: HouseState => h.componentStates.collect {
          case b: BatteryState => b.currentCharge
        }
        .reduceOption(_ + _)
        .getOrElse(0.kwh)
    }
    if charges.isEmpty then BatteriesChargeStatistic.empty
    else BatteriesChargeStatistic(
      samples = 1L,
      totalCharge = charges.reduceOption(_ + _).getOrElse(0.kwh),
      maxCharge = charges.max
    )

final case class BatteriesChargeStatistic(
                                          samples: Long,
                                          totalCharge: Energy,
                                          maxCharge: Energy
                                         )

object BatteriesChargeStatistic:
  val empty: BatteriesChargeStatistic = BatteriesChargeStatistic(0L, Energy.Zero, Energy.Zero)

  given Monoid[BatteriesChargeStatistic] with
    def empty: BatteriesChargeStatistic = BatteriesChargeStatistic.empty
    def combine(a: BatteriesChargeStatistic, b: BatteriesChargeStatistic): BatteriesChargeStatistic =
      if a.samples == 0 then b
      else if b.samples == 0 then a
      else BatteriesChargeStatistic(
        samples = a.samples + b.samples,
        totalCharge = a.totalCharge + b.totalCharge,
        maxCharge = a.maxCharge max b.maxCharge
      )

  extension (s: BatteriesChargeStatistic)
    def averageCharge: Energy =
      if s.samples > 0 then Energy(s.totalCharge.toDouble / s.samples) else Energy.Zero
