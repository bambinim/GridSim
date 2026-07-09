package org.gridsim.core.statistics

import org.gridsim.core.observability.SimulationData.SimulationSnapshot

object NetFlowSampler:
  def sample(snapshot: SimulationSnapshot): NetFlowSample =
    NetFlowSample(
      tick = snapshot.environment.time,
      netFlowKwh = snapshot.entityFlows.values.map(_.value).sum
    )
