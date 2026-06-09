package org.gridsim.core.validation

import cats.data.Validated
import cats.data.ValidatedNec
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.error.DomainError.*

/**
 * A type class for validating domain entities.
 *
 * @tparam A The type of the entity to be validated.
 */
trait Validator[A]:
  /**
   * Validate the given entity.
   *
   * @param a The entity to validate
   * @return A ValidatedNec containing the accumulated DomainError if validation failed
   * or the original entity if validation succeeds.
   */
  def validate(a: A): ValidatedNec[DomainError, A]

object Validator:
  /**
   * Allows calling '.validate' directly on any entity that has a
   * given [[Validator[A]] in scope.
   */
  extension [A](a: A)(using v: Validator[A])
    def validate: ValidatedNec[DomainError, A] = v.validate(a)

  /**
   * These extensions simplify the validation
   * by providing reusable rules.
   */
  extension (value: Double)
    /**
     * Ensures the value is positive.
     *
     * @param field The name of the field being validated(used for error messages).
     * @return Validated value or a [[ValueMustBePositive]] error.
     */
    def mustBePositive(field: String): ValidatedNec[DomainError, Double] =
      Validated.condNec(value > 0, value, ValueMustBePositive(field, value))
    /**
     * Checks that the value falls within the specified [min, max] range.
     *
     * @param field  The name of the field being validated(used for error messages).
     * @param min The lower bound.
     * @param max The upper bound.
     * @return Validated value or a [[OutOfRange]] error.
     */
    def mustBeInRange(field: String, min: Double, max: Double): ValidatedNec[DomainError, Double] =
      Validated.condNec(value >= min && value <= max, value, OutOfRange(field, value, min, max))

  // TODO can be defined better rules for validate the ID.
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
