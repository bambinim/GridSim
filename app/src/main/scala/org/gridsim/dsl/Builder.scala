package org.gridsim.dsl

import org.gridsim.core.model.{GridEntity, GridEntityState}
import cats.data.ValidatedNec

trait Builder[E <: GridEntity, S <: GridEntityState]:
  def build(): ValidatedNec[String, (E, S)]
  def flatMap(): ValidatedNec[String, (E, S)] = build()
