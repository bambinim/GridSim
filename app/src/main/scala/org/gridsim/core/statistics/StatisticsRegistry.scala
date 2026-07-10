package org.gridsim.core.statistics

import org.gridsim.core.observability.SimulationData.{EntityFlowsData, SimulationSnapshot}

enum StatKey[A](val name: String):
  case SimStats extends StatKey[FlowStatistic]("simulationStatistics")
  case NetFlowHist extends StatKey[NetFlowHistoryStatistic]("netFlowHistory")

object StatisticsRegistry:

  private val flowStatisticFold: Fold[SimulationSnapshot, FlowStatistic] =
    Fold.monoidal(FlowSampler.sample)
      .contramap(snapshot => EntityFlowsData(snapshot.entityFlows))

  private val netFlowHistoryStatisticFold: Fold[SimulationSnapshot, NetFlowHistoryStatistic] =
    Fold.unfold[SimulationSnapshot, NetFlowHistoryStatistic, NetFlowHistoryStatistic](NetFlowHistoryStatistic.empty(capacity = 200))(
      (history, snapshot) => history.record(NetFlowSampler.sample(snapshot))
    )(identity)

  private def allStatistics: List[Registration[SimulationSnapshot, ?]] = List(
    Registration(StatKey.SimStats, flowStatisticFold),
    Registration(StatKey.NetFlowHist, netFlowHistoryStatisticFold)
  )

  val engine: Fold[SimulationSnapshot, StatsBoard] = StatisticsEngine.build(allStatistics)
