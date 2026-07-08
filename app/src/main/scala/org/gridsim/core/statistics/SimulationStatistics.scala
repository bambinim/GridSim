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
    def averageNetFlow: Double =
      if s.ticks == 0 then 0.0
      else (s.totalExported.toDouble - s.totalImported.toDouble) / s.ticks
