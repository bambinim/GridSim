package org.gridsim.core.statistics

import java.time.LocalDateTime
import org.gridsim.core.observability.SimulationData.SimulationSnapshot

/** A net flow sample calculator */
object NetFlowSampler:
  def sample(snapshot: SimulationSnapshot): NetFlowSample =
    NetFlowSample(
      dateTime = snapshot.environment.currentDateTime,
      netFlowKwh = snapshot.entityFlows.values.map(_.value).sum
    )

/**
 * A single point in a [[NetFlowHistoryStatistic]], the net flow recorded at a given simulation time.
 *
 * @param dateTime the simulation date the sample was taken at
 * @param netFlowKwh net flow (kWh, signed: if positive it is export, if negative it is import)
 */
final case class NetFlowSample(dateTime: LocalDateTime, netFlowKwh: Double)

/**
 * A bounded, chronologically ordered window of recent net-flow samples.
 *
 * @param samples net flow values in a [[NetFlowSample]]
 * @param capacity maximum number of samples retained; older samples are dropped first (FIFO)
 */
final case class NetFlowHistoryStatistic private(samples: Vector[NetFlowSample], capacity: Int):
  def record(sample: NetFlowSample): NetFlowHistoryStatistic =
    val updated = samples :+ sample
    if updated.length > capacity then
      copy(samples = updated.drop(updated.length - capacity))
    else
      copy(samples = updated)

object NetFlowHistoryStatistic:
  def empty(capacity: Int): NetFlowHistoryStatistic =
    NetFlowHistoryStatistic(Vector.empty, capacity)
