package org.gridsim.core.solver

import org.gridsim.core.common.*
import org.gridsim.core.common.Flow.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.gridsim.core.model.network.{
  Cable,
  CableConnections,
  ExternalGrid,
  GridGraph
}
import org.gridsim.core.model.GridEntity
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import scala.collection.{Map => CMap}

/** A minimal GridEntity stub for testing purposes. */
case class TestEntity(id: String) extends GridEntity

@RunWith(classOf[JUnitRunner])
class SimplePowerFlowSolverSpec extends AnyFlatSpec with Matchers {

  private val tolerance = 1e-9

  // ─── Helpers ──────────────────────────────────────────────────────────

  private def mkCable(a: String, b: String, cap: Double = 1000.0): Cable =
    Cable(CableConnections(a, b), cap.kw)

  private def surplus(kWh: Double): Flow[Energy] = Flow.Surplus(kWh.kwh)
  private def deficit(kWh: Double): Flow[Energy] = Flow.Deficit(kWh.kwh)

  private def assertCableLoad(
      result: CMap[Cable, Energy],
      cable: Cable,
      expected: Double
  ): Unit =
    result(cable).toDouble shouldBe expected +- tolerance

  // ─── Single house + grid ──────────────────────────────────────────────

  "SimplePowerFlowSolver" should "route a single house's surplus through the connecting cable" in {
    val grid = ExternalGrid("grid")
    val house = TestEntity("h1")
    val cable = mkCable("grid", "h1")
    val graph = GridGraph(List(grid, house), List(cable))
    val flows = Map("h1" -> surplus(10.0))

    val result = SimplePowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cable, 10.0)
  }

  it should "route a single house's deficit through the connecting cable" in {
    val grid = ExternalGrid("grid")
    val house = TestEntity("h1")
    val cable = mkCable("grid", "h1")
    val graph = GridGraph(List(grid, house), List(cable))
    val flows = Map("h1" -> deficit(7.5))

    val result = SimplePowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cable, 7.5)
  }

  // ─── Balanced grid ────────────────────────────────────────────────────

  it should "assign zero load when a node is balanced" in {
    val grid = ExternalGrid("grid")
    val house = TestEntity("h1")
    val cable = mkCable("grid", "h1")
    val graph = GridGraph(List(grid, house), List(cable))
    val flows = Map("h1" -> Flow.Balanced)

    val result = SimplePowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cable, 0.0)
  }

  it should "assign zero load when all nodes cancel out in a star topology" in {
    val grid = ExternalGrid("grid")
    val h1 = TestEntity("h1")
    val h2 = TestEntity("h2")
    val c1 = mkCable("grid", "h1")
    val c2 = mkCable("grid", "h2")
    val graph = GridGraph(List(grid, h1, h2), List(c1, c2))
    // h1 produces +10, h2 consumes -10 → net = 0, but cables still carry individual flows
    val flows = Map("h1" -> surplus(10.0), "h2" -> deficit(10.0))

    val result = SimplePowerFlowSolver(graph).solve(flows)

    // Each cable carries the flow of the entity it connects (not zero!)
    assertCableLoad(result, c1, 10.0)
    assertCableLoad(result, c2, 10.0)
  }

  // ─── Star topology ────────────────────────────────────────────────────

  it should "carry independent flows in a star topology" in {
    val grid = ExternalGrid("grid")
    val h1 = TestEntity("h1")
    val h2 = TestEntity("h2")
    val h3 = TestEntity("h3")
    val c1 = mkCable("grid", "h1")
    val c2 = mkCable("grid", "h2")
    val c3 = mkCable("grid", "h3")
    val graph = GridGraph(List(grid, h1, h2, h3), List(c1, c2, c3))
    val flows = Map(
      "h1" -> surplus(5.0),
      "h2" -> deficit(3.0),
      "h3" -> surplus(8.0)
    )

    val result = SimplePowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, c1, 5.0)
    assertCableLoad(result, c2, 3.0)
    assertCableLoad(result, c3, 8.0)
  }

  // ─── Chain topology ───────────────────────────────────────────────────

  it should "aggregate flows in a chain topology (grid → h1 → h2)" in {
    val grid = ExternalGrid("grid")
    val h1 = TestEntity("h1")
    val h2 = TestEntity("h2")
    val c1 = mkCable("grid", "h1")
    val c2 = mkCable("h1", "h2")
    val graph = GridGraph(List(grid, h1, h2), List(c1, c2))
    val flows = Map(
      "h1" -> surplus(3.0),
      "h2" -> surplus(7.0)
    )

    val result = SimplePowerFlowSolver(graph).solve(flows)

    // c2 carries only h2's flow (7.0)
    assertCableLoad(result, c2, 7.0)
    // c1 carries h1 + h2 = 3 + 7 = 10.0
    assertCableLoad(result, c1, 10.0)
  }

  it should "handle mixed surplus/deficit in a chain (grid → h1 → h2)" in {
    val grid = ExternalGrid("grid")
    val h1 = TestEntity("h1")
    val h2 = TestEntity("h2")
    val c1 = mkCable("grid", "h1")
    val c2 = mkCable("h1", "h2")
    val graph = GridGraph(List(grid, h1, h2), List(c1, c2))
    val flows = Map(
      "h1" -> surplus(10.0), // h1 produces +10
      "h2" -> deficit(4.0) // h2 consumes -4
    )

    val result = SimplePowerFlowSolver(graph).solve(flows)

    // c2 carries h2's flow: |-4| = 4
    assertCableLoad(result, c2, 4.0)
    // c1 carries h1 + h2 = 10 + (-4) = |6| = 6
    assertCableLoad(result, c1, 6.0)
  }

  // ─── Deeper chain ─────────────────────────────────────────────────────

  it should "handle a 4-node chain (grid → a → b → c)" in {
    val grid = ExternalGrid("grid")
    val a = TestEntity("a")
    val b = TestEntity("b")
    val c = TestEntity("c")
    val cGA = mkCable("grid", "a")
    val cAB = mkCable("a", "b")
    val cBC = mkCable("b", "c")
    val graph = GridGraph(List(grid, a, b, c), List(cGA, cAB, cBC))
    val flows = Map(
      "a" -> surplus(2.0),
      "b" -> deficit(5.0),
      "c" -> surplus(1.0)
    )

    val result = SimplePowerFlowSolver(graph).solve(flows)

    // cBC carries subtree(c) = |1| = 1
    assertCableLoad(result, cBC, 1.0)
    // cAB carries subtree(b) = b + c = -5 + 1 = |-4| = 4
    assertCableLoad(result, cAB, 4.0)
    // cGA carries subtree(a) = a + b + c = 2 + (-5) + 1 = |-2| = 2
    assertCableLoad(result, cGA, 2.0)
  }

  // ─── Tree with branching ──────────────────────────────────────────────

  it should "handle a branching tree (grid → hub, hub → h1, hub → h2)" in {
    val grid = ExternalGrid("grid")
    val hub = TestEntity("hub")
    val h1 = TestEntity("h1")
    val h2 = TestEntity("h2")
    val cGH = mkCable("grid", "hub")
    val cH1 = mkCable("hub", "h1")
    val cH2 = mkCable("hub", "h2")
    val graph = GridGraph(List(grid, hub, h1, h2), List(cGH, cH1, cH2))
    val flows = Map(
      "hub" -> Flow.Balanced,
      "h1" -> surplus(6.0),
      "h2" -> deficit(2.0)
    )

    val result = SimplePowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cH1, 6.0)
    assertCableLoad(result, cH2, 2.0)
    // cGH carries hub + h1 + h2 = 0 + 6 + (-2) = |4| = 4
    assertCableLoad(result, cGH, 4.0)
  }

  // ─── Nodes missing from entityFlowMap ─────────────────────────────────

  it should "treat nodes absent from entityFlowMap as having zero flow" in {
    val grid = ExternalGrid("grid")
    val h1 = TestEntity("h1")
    val h2 = TestEntity("h2")
    val c1 = mkCable("grid", "h1")
    val c2 = mkCable("h1", "h2")
    val graph = GridGraph(List(grid, h1, h2), List(c1, c2))
    // Only h2 has a flow; h1 is absent from the map
    val flows = Map("h2" -> surplus(5.0))

    val result = SimplePowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, c2, 5.0)
    // c1 carries 0 (h1) + 5 (h2) = 5
    assertCableLoad(result, c1, 5.0)
  }

  // ─── ExternalGrid flow ignored ────────────────────────────────────────

  it should "ignore any flow entry for the ExternalGrid node" in {
    val grid = ExternalGrid("grid")
    val h1 = TestEntity("h1")
    val cable = mkCable("grid", "h1")
    val graph = GridGraph(List(grid, h1), List(cable))
    // Even if someone puts a flow for "grid", it must be ignored
    val flows = Map(
      "grid" -> surplus(999.0),
      "h1" -> surplus(3.0)
    )

    val result = SimplePowerFlowSolver(graph).solve(flows)

    // Cable should carry only h1's flow, not grid's
    assertCableLoad(result, cable, 3.0)
  }

  // ─── Error case: no ExternalGrid ──────────────────────────────────────

  it should "throw an exception when no ExternalGrid node is present" in {
    val h1 = TestEntity("h1")
    val h2 = TestEntity("h2")
    val cable = mkCable("h1", "h2")
    val graph = GridGraph(List(h1, h2), List(cable))

    an[IllegalArgumentException] should be thrownBy {
      SimplePowerFlowSolver(graph).solve(Map.empty)
    }
  }

  // ─── Cable direction doesn't matter ───────────────────────────────────

  it should "work regardless of cable connection order (b, a) vs (a, b)" in {
    val grid = ExternalGrid("grid")
    val h1 = TestEntity("h1")
    // Cable defined as (h1, grid) instead of (grid, h1)
    val cable = mkCable("h1", "grid")
    val graph = GridGraph(List(grid, h1), List(cable))
    val flows = Map("h1" -> deficit(4.0))

    val result = SimplePowerFlowSolver(graph).solve(flows)

    assertCableLoad(result, cable, 4.0)
  }
}
