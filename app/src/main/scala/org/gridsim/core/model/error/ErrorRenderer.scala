package org.gridsim.core.model.error

import cats.Show
import org.gridsim.core.model.error.DomainError.{OutOfRange, ValueMustBePositive}
object ErrorRenderer:
  given Show[DomainError] = Show.show {
    case DomainError.InvalidId(f, s) =>
      s"[ERROR] Identifier '$f' for $s is invalid"
    case ValueMustBePositive(f, v) =>
      s"[ERROR] Field '$f' cannot be negative. Provided: $v"
    case OutOfRange(f, v, min, max) =>
      s"[Error] Field '$f' must be in range between $min and $max. Provided: $v"
  }

