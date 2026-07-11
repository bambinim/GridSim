package org.gridsim.statistics

import cats.kernel.Monoid
import org.gridsim.core.common.{Energy, kwh}
import org.gridsim.core.observability.SimulationData.EntityFlowsData

/** A flow sample calculator */
object FlowSampler:
  def sample(flowsData: EntityFlowsData): FlowStatistic =
    val netKwh = flowsData.flows.values.map(_.value).sum
    val imported = if netKwh < 0 then Energy(-netKwh) else Energy.Zero
    val exported = if netKwh > 0 then Energy(netKwh) else Energy.Zero
    FlowStatistic(1L, imported, exported, imported, exported)

final case class FlowStatistic(
                                ticks: Long,
                                totalImported: Energy,
                                totalExported: Energy,
                                peakImport: Energy,
                                peakExport: Energy
                              )

object FlowStatistic:
  val empty: FlowStatistic =
    FlowStatistic(0L, Energy.Zero, Energy.Zero, Energy.Zero, Energy.Zero)

  given Monoid[FlowStatistic] with
    def empty: FlowStatistic = FlowStatistic.empty
    def combine(a: FlowStatistic, b: FlowStatistic) =
      FlowStatistic(
        ticks = a.ticks + b.ticks,
        totalImported = a.totalImported + b.totalImported,
        totalExported = a.totalExported + b.totalExported,
        peakImport = a.peakImport max b.peakImport,
        peakExport = a.peakExport max b.peakExport
      )

  extension (s: FlowStatistic)
    def averageNetFlow: Energy = s.ticks match
      case ticks if ticks > 0 => ((s.totalExported.toDouble - s.totalImported.toDouble) / ticks).kwh
      case _ => 0.0.kwh
