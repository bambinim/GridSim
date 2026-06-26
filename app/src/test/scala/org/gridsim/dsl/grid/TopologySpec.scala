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
