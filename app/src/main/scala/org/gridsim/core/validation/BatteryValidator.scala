package org.gridsim.core.validation

import cats.data.ValidatedNec
import cats.syntax.all.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.validation.Validator.*

/**
 * Defines the physical rules for a [[Battery]] component.
 * Ensures that both its static configuration and dynamic [[BatteryState]]
 * are coherent and physically possible.
 */
object BatteryValidator:

  given Validator[(Battery, BatteryState)] with
    def validate(pair: (Battery, BatteryState)): ValidatedNec[DomainError, (Battery, BatteryState)] =
      val (entity, state) = pair
      (
        validateCoherence(entity, state),
        validateBatteryEntity(entity),
        validateBatteryState(entity, state)
      ).mapN((_, _, _) => pair)

    private def validateCoherence(entity: Battery, state: BatteryState): ValidatedNec[DomainError, Unit] =
      if entity.id == state.entityId then ().validNec
      else DomainError.IdNotFound(entity.id).invalidNec

    private def validateBatteryEntity(b: Battery): ValidatedNec[DomainError, Battery] =
      (
        b.maxCapacity.toDouble.mustBePositive("Capacity"),
        b.maxPowerCharge.toDouble.mustBePositive("Max Power Charge"),
        b.maxPowerDischarge.toDouble.mustBePositive("Max Power Discharge"),
        b.minSoC.mustBeInRange("Min SoC", 0.0, 1.0)
      ).mapN((_, _, _, _) => b)

    private def validateBatteryState(entity: Battery, state: BatteryState): ValidatedNec[DomainError, BatteryState] =
      state.currentCharge.toDouble.mustBeInRange(
        "Current Charge",
        0.0,
        entity.maxCapacity.toDouble
      ).map(_ => state)
