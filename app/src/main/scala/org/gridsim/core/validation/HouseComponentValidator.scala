package org.gridsim.core.validation

import cats.data.ValidatedNec
import org.gridsim.core.model.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.battery.Battery
import org.gridsim.core.validation.Validator.validate
import org.gridsim.core.validation.BatteryValidator.given

/**
 * Acts as a dispatcher for all entities that can be in a house.
 */
object HouseComponentValidator:
  /**
   * The implicit [[Validator]] instance for components that can be in a house.
   */
  given houseComponentValidator: Validator[GridEntity & CanBeInHouse] with
    def validate(c: GridEntity & CanBeInHouse): ValidatedNec[DomainError, GridEntity & CanBeInHouse] =
      c match
        case b: Battery =>
          b.validate
        case other =>
          cats.data.Validated.Valid(other)

