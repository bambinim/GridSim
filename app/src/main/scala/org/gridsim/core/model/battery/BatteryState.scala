package org.gridsim.core.model.battery

import cats.syntax.all.*
import org.gridsim.core.common.Units.Energy
import org.gridsim.core.model.battery.Battery.ValidationResult

case class BatteryState(currentCharge: Energy)

object BatteryState:
  def validate(state: BatteryState, spec: BatterySpecification): ValidationResult[BatteryState] =
    if state.currentCharge >= Energy.Zero && state.currentCharge <= spec.capacity then state.validNec
    else "Error: current charge of the battery must be between 0 and Capacity".invalidNec
