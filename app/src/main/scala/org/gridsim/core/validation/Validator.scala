package org.gridsim.core.validation

import cats.data.Validated
import cats.data.ValidatedNec
import org.gridsim.core.model.error.DomainError

trait Validator[A]:
  def validate(a: A): ValidatedNec[DomainError, A]

object Validator:
  extension [A](a: A)(using v: Validator[A])
    def validate: ValidatedNec[DomainError, A] = v.validate(a)

  extension (value: Double)
    def mustBePositive(field: String): ValidatedNec[DomainError, Double] =
      Validated.condNec(value > 0, value, DomainError.ValueMustBePositive(field, value))

    def mustBeInRange(field: String, min: Double, max: Double): ValidatedNec[DomainError, Double] =
      Validated.condNec(value >= min && value <= max, value, DomainError.OutOfRange(field, value, min, max))

  extension (value: String)
    def mustBeValid(field: String): ValidatedNec[DomainError, String] =
      Validated.condNec(value.length >= 3, value, DomainError.InvalidId(field, value))
