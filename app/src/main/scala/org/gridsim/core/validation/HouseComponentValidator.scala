package org.gridsim.core.validation

import cats.data.ValidatedNec

import org.gridsim.core.model.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator.validate
import org.gridsim.core.model.battery.Battery
import org.gridsim.core.validation.BatteryValidator.given
import org.gridsim.core.model.SolarPanel
import org.gridsim.core.validation.SolarPanelValidator.given

/**
 * Acts as a dispatcher for all entities that can be in a house.
 */
object HouseComponentValidator:

  /**
   * The implicit [[Validator]] instance for storage components.
   */
  given storageValidator: Validator[Storage] with
    def validate(storage: Storage): ValidatedNec[DomainError, Storage] = storage match
      case battery: Battery => battery.validate
      case other => cats.data.Validated.validNec(other)

  /**
   * The implicit [[Validator]] instance for producer components.
   */
  given producerValidator: Validator[Producer] with
    def validate(producer: Producer): ValidatedNec[DomainError, Producer] = producer match
      case panel: SolarPanel => panel.validate
      case other => cats.data.Validated.validNec(other)
