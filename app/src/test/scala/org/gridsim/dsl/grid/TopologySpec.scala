package org.gridsim.dsl.grid

import org.gridsim.core.model.GridEntity
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.gridsim.core.common.kw
import org.gridsim.dsl.grid.Topology.*
import org.gridsim.core.model.network.{Cable, CableConnections}

@RunWith(classOf[JUnitRunner])
class TopologySpec extends AnyFlatSpec with Matchers:

  case class DummyEntity(id: String) extends GridEntity

  val e1 = DummyEntity("e1")
  val e2 = DummyEntity("e2")

  "A GridEntity" should "be connectable to another one with a cable" in:
    given ctx: TopologyBuilderContext = new TopologyBuilderContext()
    e1 <-- 10.kw --> e2
    ctx.cables.head shouldBe Cable(CableConnections("e1", "e2"), 10.kw)

  it should "also be connectable to an entity id" in:
    given ctx: TopologyBuilderContext = new TopologyBuilderContext()
    e1 <-- 10.kw --> "e2"
    ctx.cables.head shouldBe Cable(CableConnections("e1", "e2"), 10.kw)

  "A String representing an entity id" should "be connectable to a grid entity" in:
    given ctx: TopologyBuilderContext = new TopologyBuilderContext()
    "e1" <-- 10.kw --> e2
    ctx.cables.head shouldBe Cable(CableConnections("e1", "e2"), 10.kw)

  it should "also be connectable to an entity id" in:
    given ctx: TopologyBuilderContext = new TopologyBuilderContext()
    "e1" <-- 10.kw --> "e2"
    ctx.cables.head shouldBe Cable(CableConnections("e1", "e2"), 10.kw)

  "CableConnections" should "be equal to another if they have the same endpoints regardless of order" in:
    val c1 = CableConnections("nodeA", "nodeB")
    val c2 = CableConnections("nodeB", "nodeA")
    val c3 = CableConnections("nodeA", "nodeB")

    c1 shouldEqual c2
    c1 shouldEqual c3
    c1.hashCode() shouldEqual c2.hashCode()

  it should "not be equal to a connection with different endpoints" in:
    val c1 = CableConnections("nodeA", "nodeB")
    val c2 = CableConnections("nodeA", "nodeC")

    c1 shouldNot equal(c2)

  it should "not be equal to an object of a different type" in:
    val c1 = CableConnections("nodeA", "nodeB")

    c1 shouldNot equal("just a string")
