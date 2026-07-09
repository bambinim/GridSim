package org.gridsim.core.statistics

import scala.concurrent.duration.FiniteDuration

/**
 * A single point in a [[NetFlowHistory]]: the net flow recorded at a given
 * simulation time.
 *
 * @param tick the simulation time the sample was taken at
 * @param netFlowKwh net flow (kWh, signed: positive = export, negative = import)
 */
final case class NetFlowSample(tick: FiniteDuration, netFlowKwh: Double)

/**
 * A bounded, chronologically-ordered window of recent net-flow samples.
 *
 * Unlike [[SimulationStatistics]], this is not a monoid: a sample can't be
 * derived from combining others, so the full (bounded) sequence has to be
 * retained. Capacity is enforced on every [[record]] call, so the buffer
 * never grows past `capacity` regardless of how many ticks are recorded.
 *
 * @param samples net flow values in a [[NetFlowSample]]
 * @param capacity maximum number of samples retained; older samples are
 *                 dropped first (FIFO)
 */
final case class NetFlowHistory private (samples: Vector[NetFlowSample], capacity: Int):
  def record(sample: NetFlowSample): NetFlowHistory =
    val updated = samples :+ sample
    if updated.length > capacity then
      copy(samples = updated.drop(updated.length - capacity))
    else
      copy(samples = updated)

object NetFlowHistory:
  def empty(capacity: Int): NetFlowHistory =
    NetFlowHistory(Vector.empty, capacity)
