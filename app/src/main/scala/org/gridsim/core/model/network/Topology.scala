package org.gridsim.core.model.network

import org.gridsim.core.common.Power
import org.gridsim.core.model.GridEntity

import scala.collection.{Iterable, Set}
import cats.data.ValidatedNec
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.validation.Validator

/** Represents an undirected connection between two nodes in the grid.
  *
  * Equality is order-independent:
  * `CableConnections("a", "b") == CableConnections("b", "a")`.
  *
  * @param n1
  *   One endpoint of the connection.
  * @param n2
  *   The other endpoint of the connection.
  */
case class CableConnections(n1: String, n2: String):
  override def equals(obj: Any): Boolean = obj match
    case that: CableConnections =>
      (this.n1 == that.n1 && this.n2 == that.n2) ||
      (this.n1 == that.n2 && this.n2 == that.n1)
    case _ => false

  override def hashCode(): Int =
    n1.hashCode + n2.hashCode

case class Cable(connections: CableConnections, maxCapacity: Power)

case class GridGraph(nodes: Iterable[GridEntity], cables: Iterable[Cable])

case class ExternalGrid(id: String) extends GridEntity

object GridGraph:

  given Validator[GridGraph] with
    def validate(gridGraph: GridGraph): ValidatedNec[DomainError, GridGraph] =
      TopologyValidation.validate(gridGraph)

  def make(
      node: Iterable[GridEntity],
      cables: Iterable[Cable]
  ): ValidatedNec[DomainError, GridGraph] =
    val validator: Validator[GridGraph] = summon[Validator[GridGraph]]
    validator.validate(GridGraph(node, cables))
