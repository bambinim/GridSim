package org.gridsim.core.validation

import cats.data.ValidatedNec
import cats.syntax.all.*
import org.gridsim.core.model.battery.{Battery, BatterySpecification, BatteryState}
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator.*

/**
 * Defines the physical rules for a [[Battery]] component.
 * Ensures that both its [[BatterySpecification]] and [[BatteryState]]
 * are coherent and physically possible.
 */
object BatteryValidator:
  given Validator[Battery] with
    def validate(battery: Battery): ValidatedNec[DomainError, Battery] =
      (
        validateSpec(battery.spec),
        validateState(battery.state, battery.spec)
      ).mapN((_, _) => battery)

    /**
     * Validates the state of the [[Battery]] against its hardware limitation.
     *
     * Physical Rules:
     * 1. A [[Battery]] cannot have negative charge.
     * 2. A [[Battery]] cannot hold more charge than its maximum capacity.
     */
    private def validateState(state: BatteryState, spec: BatterySpecification): ValidatedNec[DomainError, BatteryState] =
      state.currentCharge.toDouble.mustBeInRange("Current Charge", 0.0, spec.capacity.toDouble).map( _ => state)

    /**
     * Validates the static hardware specifications of the battery.
     *
     * Physical Rules:
     * 1. Hardware capacities and power transfer rates must be strictly positive.
     * 2. The Minimum State of Charge (minSoC) represents a percentage and must be in [0.0, 1.0].
     */
    private def validateSpec(spec: BatterySpecification): ValidatedNec[DomainError, BatterySpecification] =
      (
        spec.capacity.toDouble.mustBePositive("Capacity"),
        spec.maxPowerCharge.toDouble.mustBePositive("Max Power Charge"),
        spec.maxPowerDischarge.toDouble.mustBePositive("Max Power Discharge"),
        spec.minSoC.mustBeInRange("Min SoC", 0.0, 1.0)
      ).mapN((_, _, _, _) => spec)
