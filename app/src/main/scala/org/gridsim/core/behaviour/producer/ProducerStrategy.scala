package org.gridsim.core.behaviour.producer

import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.{Producer, ProducerState}

import scala.concurrent.duration.FiniteDuration

/** Contract for energy producer strategies. */
trait ProducerStrategy[S <: ProducerState, P <: Producer, I]:
  /**
   * Computes the energy produced during one simulation tick.
   *
   * @param state    Current panel state.
   * @param producer The producer considered.
   * @param input    Needed to calculate the production during the tick.
   * @param delta    Duration of the tick.
   * @return Updated [[SolarPanelState]] and the generated [[Flow]].
   */
  def produce(state: S, producer: P, input: I)(using delta: FiniteDuration): (S, Flow[Energy])
