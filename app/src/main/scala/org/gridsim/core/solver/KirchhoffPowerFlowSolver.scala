package org.gridsim.core.solver

import org.gridsim.core.model.network.{Cable, CableConnections, ExternalGrid, GridGraph}
import org.gridsim.core.common.Units.{Energy, Flow}

import scala.collection.Map

/**
 * A Kirchhoff-based power flow solver for arbitrary (including meshed) micro-grids.
 *
 * Uses a DC power flow model to distribute energy flows across all cables,
 * including those forming cycles. The algorithm:
 *
 *  1. Builds the '''graph Laplacian''' (bus admittance matrix) from the cable topology.
 *     Since [[Cable]] carries no impedance information, all cables are assumed to have
 *     unit susceptance (equal impedance). This means flow distributes purely based
 *     on network topology.
 *
 *  2. Sets the [[ExternalGrid]] node as the '''reference bus''' (θ = 0). Its power
 *     injection is the residual — absorbing surplus or supplying deficit.
 *
 *  3. Solves the reduced linear system '''B_reduced · θ = P''' for the voltage angle
 *     vector θ, using Gaussian elimination with partial pivoting.
 *
 *  4. Computes the flow on each cable as '''|θ_i − θ_j| · b_ij''', where b_ij is
 *     the susceptance of the cable (1.0 by default).
 *
 * For tree topologies, this solver produces results identical to [[SimplePowerFlowSolver]].
 * For meshed topologies, it correctly splits flows across parallel paths.
 */
object KirchhoffPowerFlowSolver extends PowerFlowSolver:

  override def solve(entityFlowMap: Map[String, Flow[Energy]], graph: GridGraph): Map[Cable, Energy] =
    val nodeIds = collectNodeIds(graph)
    val refId = findReference(graph)

    if nodeIds.size <= 1 then
      // Only the reference node exists (or no nodes at all); no cables carry any flow.
      return graph.cables.map(_ -> Energy.Zero).toMap

    // Assign numeric indices: reference node gets index 0.
    val indexMap = buildIndexMap(nodeIds, refId)
    val n = nodeIds.size

    // Build the Laplacian (admittance) matrix.
    val laplacian = buildLaplacian(n, graph.cables, indexMap)

    // Build the injection vector (P). Reference node (index 0) gets P=0.
    val injections = buildInjectionVector(n, entityFlowMap, indexMap, refId)

    // Solve the reduced system (remove reference row/column 0).
    val thetaReduced = solveReducedSystem(laplacian, injections)

    // Full theta vector: theta[0] = 0 (reference), theta[i] = thetaReduced[i-1].
    val theta = new Array[Double](n)
    System.arraycopy(thetaReduced, 0, theta, 1, n - 1)

    // Compute cable flows from angle differences.
    computeCableFlows(graph.cables, theta, indexMap)

  /**
   * Collects all unique node IDs from the graph's nodes and cable endpoints.
   */
  private def collectNodeIds(graph: GridGraph): IndexedSeq[String] =
    val fromNodes = graph.nodes.map(_.id)
    val fromCables = graph.cables.flatMap(c => List(c.connections.n1, c.connections.n2))
    (fromNodes ++ fromCables).toSet.toIndexedSeq

  /**
   * Finds the [[ExternalGrid]] node ID to use as the reference bus.
   *
   * @throws IllegalArgumentException if no ExternalGrid is present.
   */
  private def findReference(graph: GridGraph): String =
    graph.nodes
      .collectFirst { case eg: ExternalGrid => eg.id }
      .getOrElse(throw IllegalArgumentException("GridGraph must contain an ExternalGrid node"))

  /**
   * Creates a bidirectional mapping between node IDs and numeric indices.
   * The reference node is always assigned index 0.
   */
  private def buildIndexMap(nodeIds: IndexedSeq[String], refId: String): scala.collection.immutable.Map[String, Int] =
    val nonRef = nodeIds.filterNot(_ == refId)
    (refId +: nonRef).zipWithIndex.toMap

  /**
   * Builds the graph Laplacian (bus admittance matrix) B.
   *
   * For each cable (i, j) with susceptance b = 1:
   *   - B[i][i] += b
   *   - B[j][j] += b
   *   - B[i][j] -= b
   *   - B[j][i] -= b
   *
   * Multiple cables between the same pair of nodes are handled correctly
   * (their susceptances accumulate).
   */
  private def buildLaplacian(n: Int, cables: Iterable[Cable], indexMap: Map[String, Int]): Array[Array[Double]] =
    val L = Array.ofDim[Double](n, n)
    cables.foreach { cable =>
      val CableConnections(aId, bId) = cable.connections
      val i = indexMap(aId)
      val j = indexMap(bId)
      val b = 1.0 // unit susceptance (no impedance info on Cable)
      L(i)(i) += b
      L(j)(j) += b
      L(i)(j) -= b
      L(j)(i) -= b
    }
    L

  /**
   * Builds the power injection vector P.
   *
   * P[i] = signed flow of node i (positive = surplus, negative = deficit).
   * The reference node (index 0) is set to 0 — its injection is the residual
   * and is determined implicitly by the system.
   */
  private def buildInjectionVector(
    n: Int,
    entityFlowMap: Map[String, Flow[Energy]],
    indexMap: Map[String, Int],
    refId: String
  ): Array[Double] =
    val P = new Array[Double](n)
    entityFlowMap.foreach { (nodeId, flow) =>
      if nodeId != refId then
        indexMap.get(nodeId).foreach { idx =>
          P(idx) = flow.value
        }
    }
    P

  /**
   * Solves the reduced linear system B_reduced · θ = P_reduced.
   *
   * Removes row 0 and column 0 (the reference bus) from the Laplacian
   * and the injection vector, then applies Gaussian elimination with
   * partial pivoting.
   *
   * @param laplacian  The full n×n Laplacian matrix.
   * @param injections The full n-element injection vector.
   * @return The (n-1)-element solution vector θ_reduced.
   */
  private def solveReducedSystem(laplacian: Array[Array[Double]], injections: Array[Double]): Array[Double] =
    val n = laplacian.length
    val m = n - 1 // reduced size

    if m == 0 then return Array.empty

    // Extract the reduced matrix (skip row 0 and col 0).
    val A = Array.tabulate(m, m)((i, j) => laplacian(i + 1)(j + 1))
    val b = Array.tabulate(m)(i => injections(i + 1))

    gaussianElimination(A, b)

  /**
   * Gaussian elimination with partial pivoting.
   *
   * Solves Ax = b in-place and returns x.
   *
   * @param A An m×m coefficient matrix (modified in place).
   * @param b An m-element right-hand side vector (modified in place).
   * @return The m-element solution vector x.
   * @throws ArithmeticException if the matrix is singular.
   */
  private def gaussianElimination(A: Array[Array[Double]], b: Array[Double]): Array[Double] =
    val m = A.length

    // Forward elimination with partial pivoting.
    for col <- 0 until m do
      // Find pivot row.
      var maxVal = math.abs(A(col)(col))
      var pivotRow = col
      for row <- col + 1 until m do
        if math.abs(A(row)(col)) > maxVal then
          maxVal = math.abs(A(row)(col))
          pivotRow = row

      if maxVal < 1e-12 then
        throw ArithmeticException("Singular matrix in Kirchhoff solver — the graph may be disconnected")

      // Swap rows if needed.
      if pivotRow != col then
        val tmpRow = A(col)
        A(col) = A(pivotRow)
        A(pivotRow) = tmpRow
        val tmpB = b(col)
        b(col) = b(pivotRow)
        b(pivotRow) = tmpB

      // Eliminate below.
      for row <- col + 1 until m do
        val factor = A(row)(col) / A(col)(col)
        for j <- col until m do
          A(row)(j) -= factor * A(col)(j)
        b(row) -= factor * b(col)

    // Back substitution.
    val x = new Array[Double](m)
    for i <- m - 1 to 0 by -1 do
      var sum = b(i)
      for j <- i + 1 until m do
        sum -= A(i)(j) * x(j)
      x(i) = sum / A(i)(i)

    x

  /**
   * Computes the absolute energy flow on each cable from the voltage angles.
   *
   * Flow on cable (i, j) = |θ_i − θ_j| × b_ij, where b_ij = 1.0 (unit susceptance).
   */
  private def computeCableFlows(
    cables: Iterable[Cable],
    theta: Array[Double],
    indexMap: Map[String, Int]
  ): Map[Cable, Energy] =
    cables.map { cable =>
      val CableConnections(aId, bId) = cable.connections
      val i = indexMap(aId)
      val j = indexMap(bId)
      val flow = math.abs(theta(i) - theta(j)) // * 1.0 (unit susceptance)
      cable -> Energy(flow)
    }.toMap
