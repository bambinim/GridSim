package org.gridsim.statistics

import org.gridsim.core.observability.SimulationData.{EntityFlowsData, EntityStatesData, SimulationSnapshot}

enum StatKey[A](val name: String):
  case FlowStatKey extends StatKey[FlowStatistic]("flowStat")
  case NetFlowHistoryStatKey extends StatKey[NetFlowHistoryStatistic]("netFlowHistory")
  case BatteryChargeStatKey extends StatKey[BatteriesChargeStatistic]("batteryCharge")
  case CableOverloadStatKey extends StatKey[CablesOverloadStatistic]("cableOverload")
  case SimTimeStatKey extends StatKey[SimulationTimeStatistic]("simulationTime")

object StatisticsRegistry:

  private val flowStatisticFold: Fold[SimulationSnapshot, FlowStatistic] =
    Fold.monoidal(FlowSampler.sample)
      .contramap(snapshot => EntityFlowsData(snapshot.entityFlows))

  private val netFlowHistoryStatisticFold: Fold[SimulationSnapshot, NetFlowHistoryStatistic] =
    Fold.unfold[SimulationSnapshot, NetFlowHistoryStatistic, NetFlowHistoryStatistic](NetFlowHistoryStatistic.empty(capacity = 200))(
      (history, snapshot) => history.record(NetFlowSampler.sample(snapshot))
    )(identity)

  private val batteryChargeFold: Fold[SimulationSnapshot, BatteriesChargeStatistic] =
    Fold.monoidal(BatteriesChargeSampler.sample)
      .contramap(snapshot => EntityStatesData(snapshot.entityStates))

  private val cableOverloadFold: Fold[SimulationSnapshot, CablesOverloadStatistic] =
    Fold.monoidal(CablesOverloadSampler.sample)

  private def allStatistics: List[Registration[SimulationSnapshot, ?]] = List(
    Registration(StatKey.FlowStatKey, flowStatisticFold),
    Registration(StatKey.NetFlowHistoryStatKey, netFlowHistoryStatisticFold),
    Registration(StatKey.BatteryChargeStatKey, batteryChargeFold),
    Registration(StatKey.CableOverloadStatKey, cableOverloadFold),
    Registration(StatKey.SimTimeStatKey, SimulationTimeStatistic.fold)
  )

  val engine: Fold[SimulationSnapshot, StatsBoard] = StatisticsEngine.build(allStatistics)
