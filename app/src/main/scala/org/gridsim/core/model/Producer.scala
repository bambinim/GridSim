package org.gridsim.core.model

import org.gridsim.core.common.Power

trait Producer extends GridEntity:
  def maxProduction: Power

trait ProducerState extends GridEntityState:
  def currentProduction: Power
