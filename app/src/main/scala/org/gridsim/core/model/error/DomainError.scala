package org.gridsim.core.model.error

enum DomainError:
  case ValueMustBePositive(field: String, value: Double)
  case OutOfRange(field: String, value: Double, min: Double, max: Double)
  case InvalidId(field: String, str: String)
