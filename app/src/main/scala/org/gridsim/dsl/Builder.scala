package org.gridsim.dsl

import org.gridsim.core.model.{GridEntity, GridEntityState}
import cats.data.ValidatedNec
import cats.Show

trait Builder[+T]:
  def build(): ValidatedNec[DSLError, T]
  def flatMap(): ValidatedNec[DSLError, T] = build()

trait GridEntityBuilder[+E <: GridEntity, +S <: GridEntityState]
    extends Builder[(E, S)]
