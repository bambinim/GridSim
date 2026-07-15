package org.gridsim.core.model.network

import cats.data.Validated
import cats.data.ValidatedNec
import cats.implicits.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.error.DomainError.TopologyError

object TopologyValidation:
  def validate(gridGraph: GridGraph): ValidatedNec[DomainError, GridGraph] =
    (
      checkExternalGrid(gridGraph),
      checkCablesAndNodesMatch(gridGraph),
      checkReachability(gridGraph),
      checkNoSelfLoops(gridGraph)
    ).mapN((_, _, _, _) => gridGraph)

  private def checkExternalGrid(
      gridGraph: GridGraph
  ): ValidatedNec[DomainError, Unit] =
    Validated.condNec(
      gridGraph.nodes.exists(_.isInstanceOf[ExternalGrid]),
      (),
      TopologyError("The grid must contain at least one ExternalGrid.")
    )

  private def checkNoSelfLoops(
      gridGraph: GridGraph
  ): ValidatedNec[DomainError, Unit] =
    gridGraph.cables
      .filter(c => c.connections.n1 == c.connections.n2)
      .toList
      .traverse_(c =>
        TopologyError(
          s"Self-loop found on node: ${c.connections.n1}"
        ).invalidNec
      )

  private def checkCablesAndNodesMatch(
      gridGraph: GridGraph
  ): ValidatedNec[DomainError, Unit] =
    val nodeIds = gridGraph.nodes.map(_.id).toSet
    val nodesInCables = gridGraph.cables
      .flatMap(c => List(c.connections.n1, c.connections.n2))
      .toSet

    val unknownNodes = nodesInCables -- nodeIds
    val disconnectedNodes = nodeIds -- nodesInCables

    val unknownValid = unknownNodes.toList.traverse_(n =>
      TopologyError(s"Cable references unknown node: $n").invalidNec
    )

    val disconnectedValid =
      if gridGraph.nodes.size <= 1 then ().validNec
      else
        disconnectedNodes.toList.traverse_(n =>
          TopologyError(s"Node is not connected to any cable: $n").invalidNec
        )

    (unknownValid, disconnectedValid).mapN((_, _) => ())

  private def checkReachability(
      gridGraph: GridGraph
  ): ValidatedNec[DomainError, Unit] =
    val externalGrids =
      gridGraph.nodes.filter(_.isInstanceOf[ExternalGrid]).map(_.id)
    externalGrids.isEmpty match
      case true  => ().validNec
      case false =>
        val adj = scala.collection.mutable
          .Map[String, List[String]]()
          .withDefaultValue(Nil)
        for (c <- gridGraph.cables) {
          adj(c.connections.n1) ::= c.connections.n2
          adj(c.connections.n2) ::= c.connections.n1
        }
        var visited = Set.empty[String]
        def dfs(node: String): Unit =
          visited(node) match
            case true  => return
            case false =>
              visited += node
              adj(node).foreach(dfs)

        externalGrids.foreach(dfs)

        val unreachable = gridGraph.nodes.map(_.id).toSet -- visited
        unreachable.toList.traverse_(n =>
          TopologyError(
            s"Node is not reachable starting from an external grid: $n"
          ).invalidNec
        )
