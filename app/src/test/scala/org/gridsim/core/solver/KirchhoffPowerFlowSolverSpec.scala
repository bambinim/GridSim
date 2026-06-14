package org.gridsim.core.solver

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.gridsim.core.model.network.{Cable, CableConnections, ExternalGrid, GridGraph}
import org.gridsim.core.common.Units.{Energy, Flow}
import org.gridsim.core.model.GridEntity
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import scala.collection.{Map => CMap}

/** Reuse the stub from SimplePowerFlowSolverSpec if needed. */
case class KTestEntity(id: String) extends GridEntity

@RunWith(classOf[JUnitRunner])
class KirchhoffPowerFlowSolverSpec extends AnyFlatSpec with Matchers {

  private val tolerance = 1e-9

  // ─── Helpers ──────────────────────────────────────────────────────────

  private def mkCable(a: String, b: String, cap: Double = 1000.0): Cable =
    Cable(CableConnections(a, b), Energy(cap))

  private def surplus(kWh: Double): Flow[Energy] = Flow.Surplus(Energy(kWh))
  private def deficit(kWh: Double): Flow[Energy] = Flow.Deficit(Energy(kWh))

  private def assertCableLoad(result: CMap[Cable, Energy], cable: Cable, expected: Double): Unit =
    result(cable).toDouble shouldBe expected +- tolerance

  // ═══════════════════════════════════════════════════════════════════════
  //  TREE TOPOLOGIES — should match SimplePowerFlowSolver exactly
  // ═══════════════════════════════════════════════════════════════════════

  "KirchhoffPowerFlowSolver" should "route a single house's surplus through the connecting cable" in {
    val grid = ExternalGrid("grid")
    val house = KTestEntity("h1")
    val cable = mkCable("grid", "h1")
    val graph = GridGraph(List(grid, house), List(cable))
    val flows = Map("h1" -> surplus(10.0))

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cable, 10.0)
  }

  it should "route a single house's deficit through the connecting cable" in {
    val grid = ExternalGrid("grid")
    val house = KTestEntity("h1")
    val cable = mkCable("grid", "h1")
    val graph = GridGraph(List(grid, house), List(cable))
    val flows = Map("h1" -> deficit(7.5))

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cable, 7.5)
  }

  it should "assign zero load when a node is balanced" in {
    val grid = ExternalGrid("grid")
    val house = KTestEntity("h1")
    val cable = mkCable("grid", "h1")
    val graph = GridGraph(List(grid, house), List(cable))
    val flows = Map("h1" -> Flow.Balanced)

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cable, 0.0)
  }

  it should "carry independent flows in a star topology" in {
    val grid = ExternalGrid("grid")
    val h1 = KTestEntity("h1")
    val h2 = KTestEntity("h2")
    val h3 = KTestEntity("h3")
    val c1 = mkCable("grid", "h1")
    val c2 = mkCable("grid", "h2")
    val c3 = mkCable("grid", "h3")
    val graph = GridGraph(List(grid, h1, h2, h3), List(c1, c2, c3))
    val flows = Map(
      "h1" -> surplus(5.0),
      "h2" -> deficit(3.0),
      "h3" -> surplus(8.0)
    )

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, c1, 5.0)
    assertCableLoad(result, c2, 3.0)
    assertCableLoad(result, c3, 8.0)
  }

  it should "aggregate flows in a chain topology (grid → h1 → h2)" in {
    val grid = ExternalGrid("grid")
    val h1 = KTestEntity("h1")
    val h2 = KTestEntity("h2")
    val c1 = mkCable("grid", "h1")
    val c2 = mkCable("h1", "h2")
    val graph = GridGraph(List(grid, h1, h2), List(c1, c2))
    val flows = Map(
      "h1" -> surplus(3.0),
      "h2" -> surplus(7.0)
    )

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, c2, 7.0)
    assertCableLoad(result, c1, 10.0)
  }

  it should "handle a 4-node chain (grid → a → b → c)" in {
    val grid = ExternalGrid("grid")
    val a = KTestEntity("a")
    val b = KTestEntity("b")
    val c = KTestEntity("c")
    val cGA = mkCable("grid", "a")
    val cAB = mkCable("a", "b")
    val cBC = mkCable("b", "c")
    val graph = GridGraph(List(grid, a, b, c), List(cGA, cAB, cBC))
    val flows = Map(
      "a" -> surplus(2.0),
      "b" -> deficit(5.0),
      "c" -> surplus(1.0)
    )

    val result =  KirchhoffPowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cBC, 1.0)
    assertCableLoad(result, cAB, 4.0)
    assertCableLoad(result, cGA, 2.0)
  }

  // ═══════════════════════════════════════════════════════════════════════
  //  MESHED (CYCLIC) TOPOLOGIES — where Kirchhoff differs from Simple
  // ═══════════════════════════════════════════════════════════════════════

  it should "split flow evenly across two parallel paths (diamond topology)" in {
    // Diamond: grid → A → B and grid → B
    // B has a surplus of 10.0, A has zero flow.
    // Two paths from B to grid:
    //   Path 1: B → A → grid (2 hops)
    //   Path 2: B → grid     (1 hop)
    //
    // With unit susceptance, the Laplacian approach gives:
    //   θ_A and θ_B are solved from:
    //     2·θ_A - θ_grid - θ_B = P_A = 0     (A connected to grid and B)
    //     2·θ_B - θ_A - θ_grid = P_B = 10    (B connected to A and grid)
    //   θ_grid = 0 (reference)
    //     2·θ_A - θ_B = 0     → θ_A = θ_B / 2
    //     -θ_A + 2·θ_B = 10   → -θ_B/2 + 2·θ_B = 10 → 3·θ_B/2 = 10 → θ_B = 20/3
    //     θ_A = 10/3
    //
    // Cable flows:
    //   grid→A: |θ_grid - θ_A| = 10/3 ≈ 3.333
    //   A→B:    |θ_A - θ_B|    = 10/3 ≈ 3.333
    //   grid→B: |θ_grid - θ_B| = 20/3 ≈ 6.667
    //
    // Verification: KCL at A: inflow from B = 10/3, outflow to grid = 10/3 ✓
    // KCL at B: injection = 10, outflow to A = 10/3, outflow to grid = 20/3, total = 10 ✓

    val grid = ExternalGrid("grid")
    val a = KTestEntity("a")
    val b = KTestEntity("b")
    val cGA = mkCable("grid", "a")
    val cAB = mkCable("a", "b")
    val cGB = mkCable("grid", "b")
    val graph = GridGraph(List(grid, a, b), List(cGA, cAB, cGB))
    val flows = Map(
      "a" -> Flow.Balanced,
      "b" -> surplus(10.0)
    )

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cGA, 10.0 / 3.0)
    assertCableLoad(result, cAB, 10.0 / 3.0)
    assertCableLoad(result, cGB, 20.0 / 3.0)
  }

  it should "satisfy KCL at every node in a triangle topology" in {
    // Triangle: grid → A, grid → B, A → B
    // A has surplus 6, B has deficit 4.
    // Net grid exchange = 6 - 4 = 2 (grid absorbs 2).
    //
    // Laplacian (3×3):
    //   grid: degree=2, connected to A,B
    //   A:    degree=2, connected to grid,B
    //   B:    degree=2, connected to grid,A
    //
    // Reduced system (remove grid row/col):
    //   [2  -1] [θ_A]   [6 ]
    //   [-1  2] [θ_B] = [-4]
    //
    // Solution: θ_A = (2·6 + 1·(-4)) / (4-1) = 8/3
    //           θ_B = (2·(-4) + 1·6) / 3 = -2/3
    //
    // Cable flows:
    //   grid→A: |0 - 8/3| = 8/3 ≈ 2.667
    //   grid→B: |0 - (-2/3)| = 2/3 ≈ 0.667
    //   A→B:    |8/3 - (-2/3)| = 10/3 ≈ 3.333
    //
    // KCL check at A: injection=6, outflow to grid=8/3, outflow to B=10/3, 8/3+10/3=18/3=6 ✓
    // KCL check at B: injection=-4, inflow from A=10/3, outflow to grid... let's check:
    //   B receives 10/3 from A, sends 2/3 to grid. Net outflow at B = -10/3 + 2/3 = -8/3.
    //   Wait: injection is -4, net cable flow into B should be 4.
    //   inflow from A = 10/3, inflow from grid = -2/3 (flow is from grid to B if θ_grid > θ_B,
    //   but θ_grid=0 > θ_B=-2/3, so flow goes grid→B, meaning B receives 2/3 from grid).
    //   Total inflow to B = 10/3 + 2/3 = 12/3 = 4 ✓ (matches deficit of 4)

    val grid = ExternalGrid("grid")
    val a = KTestEntity("a")
    val b = KTestEntity("b")
    val cGA = mkCable("grid", "a")
    val cGB = mkCable("grid", "b")
    val cAB = mkCable("a", "b")
    val graph = GridGraph(List(grid, a, b), List(cGA, cGB, cAB))
    val flows = Map(
      "a" -> surplus(6.0),
      "b" -> deficit(4.0)
    )

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cGA, 8.0 / 3.0)
    assertCableLoad(result, cGB, 2.0 / 3.0)
    assertCableLoad(result, cAB, 10.0 / 3.0)
  }

  it should "handle a fully meshed 4-node grid" in {
    // Square with diagonals: grid, A, B, C all pairwise connected.
    // Only A has a surplus of 12.0, others have zero flow.
    // This creates a highly connected mesh where flow distributes across many paths.

    val grid = ExternalGrid("grid")
    val a = KTestEntity("a")
    val b = KTestEntity("b")
    val c = KTestEntity("c")
    val cGA = mkCable("grid", "a")
    val cGB = mkCable("grid", "b")
    val cGC = mkCable("grid", "c")
    val cAB = mkCable("a", "b")
    val cAC = mkCable("a", "c")
    val cBC = mkCable("b", "c")
    val graph = GridGraph(List(grid, a, b, c), List(cGA, cGB, cGC, cAB, cAC, cBC))
    val flows = Map("a" -> surplus(12.0))

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    // Verify KCL at node A: sum of outflows = 12.0
    val flowGA = result(cGA).toDouble
    val flowAB = result(cAB).toDouble
    val flowAC = result(cAC).toDouble
    (flowGA + flowAB + flowAC) shouldBe 12.0 +- tolerance

    // All cables should have non-negative flow (they do by construction)
    result.values.foreach(e => e.toDouble should be >= 0.0)

    // The direct cable grid→A should carry more flow than indirect paths
    flowGA should be > flowAB
    flowGA should be > flowAC
  }

  // ═══════════════════════════════════════════════════════════════════════
  //  EDGE CASES
  // ═══════════════════════════════════════════════════════════════════════

  it should "handle a single node (only ExternalGrid) with no cables" in {
    val grid = ExternalGrid("grid")
    val graph = GridGraph(List(grid), Nil)
    val flows: Map[String, Flow[Energy]] = Map.empty

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    result shouldBe empty
  }

  it should "ignore any flow entry for the ExternalGrid node" in {
    val grid = ExternalGrid("grid")
    val h1 = KTestEntity("h1")
    val cable = mkCable("grid", "h1")
    val graph = GridGraph(List(grid, h1), List(cable))
    val flows = Map(
      "grid" -> surplus(999.0),
      "h1" -> surplus(3.0)
    )

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cable, 3.0)
  }

  it should "throw an exception when no ExternalGrid node is present" in {
    val h1 = KTestEntity("h1")
    val h2 = KTestEntity("h2")
    val cable = mkCable("h1", "h2")
    val graph = GridGraph(List(h1, h2), List(cable))

    an[IllegalArgumentException] should be thrownBy {
      KirchhoffPowerFlowSolver(graph).solve(Map.empty)
    }
  }

  it should "work regardless of cable connection order" in {
    val grid = ExternalGrid("grid")
    val h1 = KTestEntity("h1")
    val cable = mkCable("h1", "grid") // reversed
    val graph = GridGraph(List(grid, h1), List(cable))
    val flows = Map("h1" -> deficit(4.0))

    val result = KirchhoffPowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cable, 4.0)
  }

  // ═══════════════════════════════════════════════════════════════════════
  //  CONSISTENCY: both solvers agree on tree topologies
  // ═══════════════════════════════════════════════════════════════════════

  it should "produce results identical to SimplePowerFlowSolver on a branching tree" in {
    val grid = ExternalGrid("grid")
    val hub = KTestEntity("hub")
    val h1 = KTestEntity("h1")
    val h2 = KTestEntity("h2")
    val cGH = mkCable("grid", "hub")
    val cH1 = mkCable("hub", "h1")
    val cH2 = mkCable("hub", "h2")
    val graph = GridGraph(List(grid, hub, h1, h2), List(cGH, cH1, cH2))
    val flows: Map[String, Flow[Energy]] = Map(
      "hub" -> Flow.Balanced,
      "h1" -> surplus(6.0),
      "h2" -> deficit(2.0)
    )

    val kirchhoffResult = KirchhoffPowerFlowSolver(graph).solve(flows)
    val simpleResult = SimplePowerFlowSolver(graph).solve(flows)

    List(cGH, cH1, cH2).foreach { cable =>
      kirchhoffResult(cable).toDouble shouldBe simpleResult(cable).toDouble +- tolerance
    }
  }
}
