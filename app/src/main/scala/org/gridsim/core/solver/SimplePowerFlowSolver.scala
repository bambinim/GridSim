package org.gridsim.core.solver

import org.gridsim.core.common.*
import org.gridsim.core.model.network.{Cable, CableConnections, ExternalGrid, GridGraph}

import scala.collection.{Map, mutable}

/**
 * Represents the BFS-built tree structure.
 *
 * @param children  Map from each node to its list of child nodes.
 * @param parentOf  Map from each child node to its parent node.
 */
case class Tree(children: Map[String, List[String]], parentOf: Map[String, String])

/**
 * A tree-based power flow solver for radial (tree-topology) micro-grids.
 *
 * Treats the [[GridGraph]] as a tree rooted at the [[ExternalGrid]] node.
 * Energy flows are aggregated from the leaves towards the root using a
 * post-order traversal: each cable carries the absolute value of the
 * net subtree flow beneath it.
 *
 * The external grid node acts as the balancing node — it absorbs the global
 * surplus or supplies the global deficit. Its own flow entry in the
 * `entityFlowMap` is ignored if present.
 */
case class SimplePowerFlowSolver(graph: GridGraph, tree: Tree, root: String) extends PowerFlowSolver:

  /**
   * Post-order DFS to compute the net signed flow of the subtree rooted at each node.
   *
   * For leaf entity nodes, the subtree flow equals the node's own signed flow.
   * For internal nodes, it is the node's own flow plus the sum of all children's subtree flows.
   * The root ([[ExternalGrid]]) is assigned own-flow = 0 — it is the residual absorber/supplier,
   * not a flow contributor.
   *
   * @return A map from node ID to its signed subtree flow (positive = surplus, negative = deficit).
   */
  private def computeSubtreeFlows(
                                   root: String,
                                   tree: Tree,
                                   entityFlowMap: Map[String, Flow[Energy]]
                                 ): Map[String, Double] =
    def dfs(node: String): (Double, Map[String, Double]) =
      val childrenData = tree.children.getOrElse(node, Nil).map(dfs)
      val childrenSum = childrenData.map(_._1).sum
      val ownFlow = if node == root then 0.0 else entityFlowMap.get(node).map(_.value).getOrElse(0.0)
      val total = ownFlow + childrenSum

      val resultMap = childrenData.map(_._2).foldLeft(Map(node -> total))(_ ++ _)
      (total, resultMap)

    dfs(root)._2

  /**
   * Maps each cable to the absolute energy flowing through it.
   *
   * For each cable, identifies which endpoint is the child in the BFS tree
   * using the `parentOf` map. The cable's load is the absolute subtree flow
   * of the child endpoint.
   *
   * If neither endpoint is a child of the other (possible only in degenerate
   * graphs where a cable connects two disconnected components), the cable
   * is assigned zero flow.
   */
  private def assignCableLoads(
                                cables: Iterable[Cable],
                                subtreeFlows: Map[String, Double],
                                parentOf: Map[String, String]
                              ): Map[Cable, Energy] =
    cables.map { cable =>
      val CableConnections(a, b) = cable.connections
      val flow = parentOf.get(a) match
        case Some(parent) if parent == b =>
          // a is the child, b is the parent → cable carries subtreeFlow(a)
          subtreeFlows.getOrElse(a, 0.0).abs
        case _ =>
          parentOf.get(b) match
            case Some(parent) if parent == a =>
              // b is the child, a is the parent → cable carries subtreeFlow(b)
              subtreeFlows.getOrElse(b, 0.0).abs
            case _ =>
              // Edge not in the spanning tree (cycle edge) — assign zero
              0.0
      cable -> Energy(flow)
    }.toMap

  override def solve(entityFlowMap: Map[String, Flow[Energy]]): Map[Cable, Energy] =
    val subtreeFlows = computeSubtreeFlows(root, tree, entityFlowMap)
    assignCableLoads(graph.cables, subtreeFlows, tree.parentOf)


object SimplePowerFlowSolver:

  def apply(graph: GridGraph): PowerFlowSolver =
    val root = findRoot(graph)
    val adjacency = buildAdjacency(graph)
    val tree = buildTree(root, adjacency)
    SimplePowerFlowSolver(graph, tree, root)

  /**
   * Finds the [[ExternalGrid]] node in the graph, which serves as the tree root.
   *
   * @throws IllegalArgumentException if no ExternalGrid is present.
   */
  private def findRoot(graph: GridGraph): String =
    graph.nodes
      .collectFirst { case eg: ExternalGrid => eg.id }
      .getOrElse(throw IllegalArgumentException("GridGraph must contain an ExternalGrid node"))

  /**
   * Builds an undirected adjacency list from the cables.
   * Each cable (a, b) produces edges a → b and b → a.
   */
  private def buildAdjacency(graph: GridGraph): scala.collection.immutable.Map[String, List[String]] =
    graph.cables
      .foldLeft(scala.collection.immutable.Map.empty[String, List[String]]) { (acc, cable) =>
        val CableConnections(a, b) = cable.connections
        acc
          .updated(a, b :: acc.getOrElse(a, Nil))
          .updated(b, a :: acc.getOrElse(b, Nil))
      }

  /**
   * BFS from the root to build a spanning tree.
   *
   * Produces both a children map (parent → children) and a parentOf map
   * (child → parent) for efficient lookup in both directions.
   * Handles cycles gracefully by visiting each node at most once.
   */
  private def buildTree(root: String, adjacency: Map[String, List[String]]): Tree =
    val visited = mutable.Set(root)
    val queue = mutable.Queue(root)
    val children = mutable.Map.empty[String, List[String]]
    val parentOf = mutable.Map.empty[String, String]

    while queue.nonEmpty do
      val node = queue.dequeue()
      val nodeChildren = adjacency.getOrElse(node, Nil).filterNot(visited.contains)
      children(node) = nodeChildren
      nodeChildren.foreach(c => parentOf(c) = node)
      visited ++= nodeChildren
      queue ++= nodeChildren

    Tree(children.toMap, parentOf.toMap)
