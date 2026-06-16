package org.gridsim.core.model.network

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.gridsim.core.common.Units.Energy
import org.gridsim.core.model.GridEntity
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

case class GridEntityStub(id: String) extends GridEntity

@RunWith(classOf[JUnitRunner])
class CablesSpec extends AnyFlatSpec with Matchers:
  // ─── CableConnections ─────────────────────────────────────────────────

  "CableConnections" should "be constructible via the companion apply method" in:
    val conn = CableConnections("a", "b")
    conn.n1 shouldBe "a"
    conn.n2 shouldBe "b"

  it should "support order-independent structural equality" in:
    CableConnections("x", "y") shouldBe CableConnections("x", "y")
    CableConnections("x", "y") shouldBe CableConnections("y", "x")
    CableConnections("x", "y") should not be CableConnections("x", "z")

  // ─── Cable ────────────────────────────────────────────────────────────

  "Cable" should "store its connections and max capacity" in:
    val cable = Cable(CableConnections("n1", "n2"), Energy(100.0))
    cable.connections shouldBe CableConnections("n1", "n2")
    cable.maxCapacity.toDouble shouldBe 100.0

  it should "support structural equality" in:
    val a = Cable(CableConnections("a", "b"), Energy(50.0))
    val b = Cable(CableConnections("a", "b"), Energy(50.0))
    a shouldBe b

  it should "not be equal when connections differ" in:
    val a = Cable(CableConnections("a", "b"), Energy(50.0))
    val b = Cable(CableConnections("a", "c"), Energy(50.0))
    a should not be b

  it should "not be equal when capacity differs" in:
    val a = Cable(CableConnections("a", "b"), Energy(50.0))
    val b = Cable(CableConnections("a", "b"), Energy(99.0))
    a should not be b


@RunWith(classOf[JUnitRunner])
class GridSpec extends AnyFlatSpec with Matchers:

  // ─── ExternalGrid ────────────────────────────────────────────────────

  "ExternalGrid" should "be a GridEntity with an id" in:
    val eg = ExternalGrid("main-grid")
    eg.id shouldBe "main-grid"
    eg shouldBe a[GridEntity]

  it should "support structural equality" in:
    ExternalGrid("g1") shouldBe ExternalGrid("g1")
    ExternalGrid("g1") should not be ExternalGrid("g2")

  // ─── GridGraph ────────────────────────────────────────────────────────

  "GridGraph" should "store nodes and cables" in:
    val nodes = List(ExternalGrid("grid"), GridEntityStub("h1"))
    val cables = List(Cable(CableConnections("grid", "h1"), Energy(100.0)))
    val graph = GridGraph(nodes, cables)

    graph.nodes should contain theSameElementsAs nodes
    graph.cables should contain theSameElementsAs cables

  it should "support an empty graph" in:
    val graph = GridGraph(Nil, Nil)
    graph.nodes shouldBe empty
    graph.cables shouldBe empty

  it should "allow multiple cables between the same nodes" in:
    val c1 = Cable(CableConnections("a", "b"), Energy(50.0))
    val c2 = Cable(CableConnections("a", "b"), Energy(100.0))
    val graph =
      GridGraph(List(GridEntityStub("a"), GridEntityStub("b")), List(c1, c2))

    graph.cables should have size 2

  it should "support structural equality" in:
    val nodes = List(ExternalGrid("grid"))
    val cables = List(Cable(CableConnections("grid", "h1"), Energy(10.0)))
    GridGraph(nodes, cables) shouldBe GridGraph(nodes, cables)
