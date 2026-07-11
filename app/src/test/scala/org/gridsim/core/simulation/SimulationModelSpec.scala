package org.gridsim.core.simulation

import org.gridsim.core.common.*
import org.gridsim.core.model.GridEntity
import org.gridsim.core.model.network.*
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SimulationModelSpec extends AnyFlatSpec with Matchers:

  private final case class TestEntity(id: String) extends GridEntity

  private val externalGrid = ExternalGrid("external-grid")
  private val house = TestEntity("house-1")
  private val cable =
    Cable(
      CableConnections(externalGrid.id, house.id),
      10.kw
    )
  private val graph =
    GridGraph(
      nodes = List(externalGrid, house),
      cables = List(cable)
    )

  "SimulationModel" should "store its grid" in:
    val model = SimulationModel(graph)

    model.grid shouldBe graph

  it should "support structural equality" in:
    SimulationModel(graph) shouldBe
      SimulationModel(graph)

  it should "distinguish models with different grid graphs" in:
    val otherGraph =
      GridGraph(
        nodes = List(externalGrid),
        cables = Nil
      )

    SimulationModel(graph) should not be
      SimulationModel(otherGraph)
