package org.gridsim.dsl

import cats.Show
import cats.syntax.all.*
import org.gridsim.core.model.error.DomainError

type DSLError = DSLBuilderError | DomainError

given Show[DSLError] = Show.show {
  case e: DSLBuilderError => e.show
  case e: DomainError     => e.show
}

enum DSLBuilderError:
  case MissingField(field: String)
  case InvalidValue(field: String, value: Any)
  case MissingBlock(block: String)

object DSLBuilderError:
  given Show[DSLBuilderError] = Show.show { e =>
    e match
      case MissingField(f)    => s"Missing field $f"
      case InvalidValue(f, v) => s"Invalid value for field $f: $v"
      case MissingBlock(b)    => s"Missing block $b"
  }
