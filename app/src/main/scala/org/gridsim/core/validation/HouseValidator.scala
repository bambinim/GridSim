package org.gridsim.core.validation

import cats.Traverse
import cats.data.ValidatedNec
import cats.syntax.all.*
import cats.implicits.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.house.{House, HouseComponent}
import org.gridsim.core.validation.Validator.*

object HouseValidator:
  def validate[F[_]: Traverse](h: House[F])(using compVal: Validator[HouseComponent]): ValidatedNec[DomainError, House[F]] =
    (
      h.id.mustBeValid("House Id"),
      h.components.traverse(compVal.validate)
    ).mapN((id, comps) => h.copy(id = id, components = comps))
