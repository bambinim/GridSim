package org.gridsim.core.validation

import cats.data.ValidatedNec
import cats.syntax.all.*
import org.gridsim.core.model.battery.{Battery, BatterySpecification, BatteryState}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator.*

object BatteryValidator extends Validator[Battery]:
  def validate(battery: Battery): ValidatedNec[DomainError, Battery] =
    (
      validateSpec(battery.spec),
      validateState(battery.state, battery.spec)
    ).mapN((_, _) => battery)

  private def validateState(state: BatteryState, spec: BatterySpecification): ValidatedNec[DomainError, BatteryState] =
    state.currentCharge.toDouble.mustBeInRange("Current Charge", 0.0, spec.capacity.toDouble).map( _ => state)

  private def validateSpec(spec: BatterySpecification): ValidatedNec[DomainError, BatterySpecification] =
    (
      spec.capacity.toDouble.mustBePositive("Capacity"),
      spec.maxPowerCharge.toDouble.mustBePositive("Max Power Charge"),
      spec.maxPowerDischarge.toDouble.mustBePositive("Max Power Discharge"),
      spec.minSoC.mustBeInRange("Min SoC", 0.0, 1.0)
    ).mapN((_, _, _, _) => spec)
