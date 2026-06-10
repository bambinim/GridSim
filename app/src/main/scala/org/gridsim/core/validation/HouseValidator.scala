package org.gridsim.core.validation

import cats.Traverse
import cats.data.ValidatedNec
import cats.syntax.all.*
import cats.implicits.*
import org.gridsim.core.model.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.house.House
import org.gridsim.core.validation.Validator.*

/**
 * Orchestrates the validation of a House entity.
 * It ensures the structural integrity of the house and recursively
 * delegates validation to all its internal components.
 */
object HouseValidator:
  /**
   * Validate a [[House]] and its internal components.
   *
   * @param h The [[House]] instance to validate.
   * @param prodVal The implicit dispatcher used to validate the producers.
   * @param storVal The implicit dispatcher used to validate the storages.
   * @tparam F The traversable container type holding the components.
   * @return A [[ValidatedNec]] containing all accumulated errors or the validated [[House]]
   */
  def validate[F[_]: Traverse](h: House[F])(using 
    prodVal: Validator[Producer], 
    storVal: Validator[Storage]
  ): ValidatedNec[DomainError, House[F]] =
    (
      h.id.mustBeValid("House Id"),
      h.producers.traverse(prodVal.validate),
      h.storages.traverse(storVal.validate)
    ).mapN((id, prods, stors) => h.copy(id = id, producers = prods, storages = stors))
