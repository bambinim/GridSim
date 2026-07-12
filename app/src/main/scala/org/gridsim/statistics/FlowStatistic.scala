package org.gridsim.statistics

import cats.kernel.Monoid
import org.gridsim.core.common.{Energy, Flow, kwh}
import org.gridsim.core.common.Energy.toFlow
import org.gridsim.core.observability.SimulationData.EntityFlowsData

object FlowSampler:
  def sample(flowsData: EntityFlowsData): FlowStatistic =
    val netEnergy = flowsData.flows.values.map(_.value).sum
    val imported = if netEnergy < 0 then Energy(-netEnergy) else Energy.Zero
    val exported = if netEnergy > 0 then Energy(netEnergy) else Energy.Zero
    FlowStatistic(1L, netEnergy.kwh.toFlow, imported, exported, imported, exported)

final case class FlowStatistic(
                                samples: Long,
                                current: Flow[Energy],
                                totalImported: Energy,
                                totalExported: Energy,
                                peakImport: Energy,
                                peakExport: Energy
                              )

object FlowStatistic:
  val empty: FlowStatistic =
    FlowStatistic(0L, Flow.Balanced, Energy.Zero, Energy.Zero, Energy.Zero, Energy.Zero)

  given Monoid[FlowStatistic] with
    def empty: FlowStatistic = FlowStatistic.empty
    def combine(a: FlowStatistic, b: FlowStatistic) =
      FlowStatistic(
        samples = a.samples + b.samples,
        current = b.current,
        totalImported = a.totalImported + b.totalImported,
        totalExported = a.totalExported + b.totalExported,
        peakImport = a.peakImport max b.peakImport,
        peakExport = a.peakExport max b.peakExport
      )

  extension (s: FlowStatistic)
    def averageNetFlow: Energy = s.samples match
      case ticks if ticks > 0 => ((s.totalExported.toDouble - s.totalImported.toDouble) / ticks).kwh
      case _ => 0.0.kwh
