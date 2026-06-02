package org.gridsim.core.model

import org.gridsim.core.common.Units.{Energy, Power}

case class Battery(spec: BatterySpecification, state: BatteryState)

case class BatterySpecification(capacity: Energy, maxPowerCharge: Power, maxPowerDischarge: Power, minSoC: Double = 0.1)

case class BatteryState(currentCharge: Energy)

object Battery:
  def makeBattery(spec: BatterySpecification, state: BatteryState): Battery =
    Battery(spec, state)
