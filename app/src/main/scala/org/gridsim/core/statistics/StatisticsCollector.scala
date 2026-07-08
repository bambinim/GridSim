package org.gridsim.core.statistics

import org.gridsim.core.common.Energy
import org.gridsim.core.observability.SimulationData.SimulationSnapshot

object StatisticsCollector:
  def collect(snapshot: SimulationSnapshot): SimulationStatistics =
    val netKwh = snapshot.entityFlows.values.map(_.value).sum
    val imported = if netKwh < 0 then Energy(-netKwh) else Energy.Zero
    val exported = if netKwh > 0 then Energy(netKwh) else Energy.Zero
    SimulationStatistics(1L, imported, exported, imported, exported)
