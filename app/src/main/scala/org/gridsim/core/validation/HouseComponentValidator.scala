package org.gridsim.core.validation

import cats.data.ValidatedNec
import cats.implicits.*
import org.gridsim.core.model.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.storage.battery.{Battery, BatteryState}
import org.gridsim.core.validation.Validator.validate
import org.gridsim.core.validation.BatteryValidator.given

/**
 * Acts as a dispatcher for all entities that can be in a house.
 */
object HouseComponentValidator:

  /**
   * The implicit [[Validator]] instance for generic house components.
   */
  given componentValidator: Validator[(GridEntity, GridEntityState)] with
    def validate(pair: (GridEntity, GridEntityState)): ValidatedNec[DomainError, (GridEntity, GridEntityState)] =
      val (entity, state) = pair

      (entity, state) match
        case (b: Battery, s: BatteryState) =>
          (b, s).validate.map { case (ent, st) => (ent: GridEntity, st: GridEntityState) }

        case (e, s) if e.id != s.entityId =>
          DomainError.InvalidId(e.id, s.entityId).invalidNec

        case _ =>
          pair.validNec
