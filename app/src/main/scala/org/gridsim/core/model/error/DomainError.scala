package org.gridsim.core.model.error

import cats.Show

/**
 * Represents the Algebraic Data Type of all possible domain-level failures.
 */
enum DomainError:
  /**
   * Indicates that a numerical value that must be strictly positive was zero or negative.
   *
   * @param field The name of the property that failed validation.
   * @param value The invalid value provided.
   */
  case ValueMustBePositive(field: String, value: Double)
  /**
   * Indicates that a value fell outside its bounds.
   *
   * @param field The name of the property.
   * @param value The invalid value provided.
   * @param min   The lower bound of the allowed range.
   * @param max   The upper bound of the allowed range.
   */
  case OutOfRange(field: String, value: Double, min: Double, max: Double)
  /**
   * Indicates that an identifier does not meet the required format or length constraints.
   *
   * @param field The name of the identifier property.
   * @param str   The invalid string that was provided.
   */
  case InvalidId(field: String, str: String)

object DomainError:
  /**
   * The implicit Cats Show instance for [[DomainError]].
   * By placing this within the companion object, the compiler will automatically
   * find it whenever `.show` is called on a [[DomainError]] instance, completely
   * eliminating the need for explicit imports in other files.
   */
  given Show[DomainError] = Show.show {
    case DomainError.InvalidId(f, s) =>
      s"[ERROR] Identifier '$f' for $s is invalid"
    case ValueMustBePositive(f, v) =>
      s"[ERROR] Field '$f' cannot be negative. Provided: $v"
    case OutOfRange(f, v, min, max) =>
      s"[Error] Field '$f' must be in range between $min and $max. Provided: $v"
  }
