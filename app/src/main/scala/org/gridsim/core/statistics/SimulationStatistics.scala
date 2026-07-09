package org.gridsim.core.statistics

import cats.kernel.Monoid
import org.gridsim.core.common.Energy

final case class SimulationStatistics(
                                       ticks: Long,
                                       totalImported: Energy,
                                       totalExported: Energy,
                                       peakImport: Energy,
                                       peakExport: Energy
                                     )

object SimulationStatistics:
  val empty: SimulationStatistics =
    SimulationStatistics(0L, Energy.Zero, Energy.Zero, Energy.Zero, Energy.Zero)

  given Monoid[SimulationStatistics] with
    def empty: SimulationStatistics = SimulationStatistics.empty
    def combine(a: SimulationStatistics, b: SimulationStatistics) =
      SimulationStatistics(
        ticks = a.ticks + b.ticks,
        totalImported = a.totalImported + b.totalImported,
        totalExported = a.totalExported + b.totalExported,
        peakImport = a.peakImport max b.peakImport,
        peakExport = a.peakExport max b.peakExport
      )

  extension (s: SimulationStatistics)
    def averageNetFlow: Double = s.ticks match
      case ticks if ticks > 0 => (s.totalExported.toDouble - s.totalImported.toDouble) / ticks
      case _ => 0.0
