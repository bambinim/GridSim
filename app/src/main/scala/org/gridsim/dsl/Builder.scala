package org.gridsim.dsl

import org.gridsim.core.model.{GridEntity, GridEntityState}
import cats.data.ValidatedNec
import cats.Show

trait Builder[E <: GridEntity, S <: GridEntityState]:
  def build(): ValidatedNec[DSLError, (E, S)]
  def flatMap(): ValidatedNec[DSLError, (E, S)] = build()
