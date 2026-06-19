package org.gridsim.core.model

/** Role trait for entities that can produce energy */
trait Producer extends GridEntity:
  def state: ProducerState

trait ProducerState
