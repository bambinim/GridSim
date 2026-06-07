package org.gridsim.core.validation

import cats.Traverse
import cats.data.ValidatedNec
import cats.syntax.all.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.house.House
import org.gridsim.core.validation.Validator.*

object HouseValidator:
  def validate[F[_]: Traverse](h: House[F]): ValidatedNec[DomainError, House[F]] =
    h.id.mustBeValid("House Id").map(_ => h)
