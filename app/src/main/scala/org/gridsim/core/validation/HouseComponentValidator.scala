package org.gridsim.core.validation

import cats.data.ValidatedNec
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.house.{HouseComponent, House}
import org.gridsim.core.model.battery.Battery
import org.gridsim.core.validation.Validator.validate
import org.gridsim.core.validation.BatteryValidator.given

/**
 * Acts as a dispatcher for all [[House]] components.
 * This object decouples the [[House]] validation logic from the specific rules
 * of individual devices.
 */
object HouseComponentValidator:
  /**
   * The implicit [[Validator]] instance for the generic HouseComponent trait.
   * It routes the validation request to the specific component validator.
   *
   * @dev Maintance Note:
   * When introducing a new type of [[HouseComponent]] you
   * must add a new case here and import its specific 'given'
   * [[Validator]].
   */
  given Validator[HouseComponent] with
    def validate(c: HouseComponent): ValidatedNec[DomainError, HouseComponent] =
      c match
        case b: Battery =>
          b.validate
        //TODO add SolarPanel for example.

