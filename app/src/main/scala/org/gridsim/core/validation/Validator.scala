package org.gridsim.core.validation

import cats.data.Validated
import cats.data.ValidatedNec
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.error.DomainError.*

/**
 * A type class for validating domain entities.
 *
 * @tparam E The type of the entity to be validated.
 */
trait Validator[E]:
  /**
   * Validate the given entity.
   *
   * @param a The entity to validate
   * @return A ValidatedNec containing the accumulated DomainError if validation failed
   * or the original entity if validation succeeds.
   */
  def validate(a: E): ValidatedNec[DomainError, E]

object Validator:

  /** Allows calling '.validate' directly on any entity that has a given [[Validator[A]] in scope. */
  extension [E](a: E)(using validator: Validator[E])
    def validate: ValidatedNec[DomainError, E] = validator.validate(a)

  /** These extensions simplify the validation by providing reusable rules. */
  extension (value: Double)
    /** Ensures the value is positive. */
    def mustBePositive(field: String): ValidatedNec[DomainError, Double] =
      Validated.condNec(value > 0, value, ValueMustBePositive(field, value))

    /** Checks that the value falls within the specified [min, max] range. */
    def mustBeInRange(field: String, min: Double, max: Double): ValidatedNec[DomainError, Double] =
      Validated.condNec(value >= min && value <= max, value, OutOfRange(field, value, min, max))

    /** Checks that the value falls within the specified (min, max) range. */
    def mustBeInRangeExclusive(field: String, min: Double, max: Double): ValidatedNec[DomainError, Double] =
      Validated.condNec(value > min && value < max, value, OutOfRange(field, value, min, max))

    /** Checks that the value falls within the specified (min, max] range. */
    def mustBeInRangeStartExclusive(field: String, min: Double, max: Double): ValidatedNec[DomainError, Double] =
      Validated.condNec(value > min && value <= max, value, OutOfRange(field, value, min, max))

    /** Checks that the value falls within the specified [min, max) range. */
    def mustBeInRangeEndExclusive(field: String, min: Double, max: Double): ValidatedNec[DomainError, Double] =
      Validated.condNec(value >= min && value < max, value, OutOfRange(field, value, min, max))
  
  extension (value: String)
    /**
     * Ensures an ID meets the minimum length requirement.
     * Business Invariant: IDs must be at least 3 characters long to avoid ambiguity.
     *
     * @param field The name of the field being validated(used for error messages).
     * @return Validated value or a [[InvalidId]] error.
     */
    def mustBeValid(field: String): ValidatedNec[DomainError, String] =
      Validated.condNec(value.length >= 3, value, InvalidId(field, value))
