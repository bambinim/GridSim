package org.gridsim.core.validation

import cats.data.ValidatedNec
import cats.syntax.all.*
import org.gridsim.core.model.House
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator.*

object HouseValidator extends Validator[House]:
  override def validate(h: House): ValidatedNec[DomainError, House] =
    h.id.mustBeValid("House Id").map(_ => h)
