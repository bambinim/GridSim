package org.gridsim.core.simulation

import org.gridsim.core.common.*
import org.gridsim.core.model.GridEntity
import org.gridsim.core.model.network.*
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class SimulationModelSpec extends AnyFlatSpec with Matchers:

  private final case class TestEntity(id: String) extends GridEntity

  private val externalGrid = ExternalGrid("external-grid")
  private val house = TestEntity("house-1")
  private val cable =
    Cable(
      CableConnections(externalGrid.id, house.id),
      10.kwh
    )
  private val graph =
    GridGraph(
      nodes = List(externalGrid, house),
      cables = List(cable)
    )

  "SimulationModel" should "store its grid and tick duration" in:
    val model = SimulationModel(graph, 15.minutes)

    model.grid shouldBe graph
    model.delta shouldBe 15.minutes

  it should "support structural equality" in:
    SimulationModel(graph, 15.minutes) shouldBe
      SimulationModel(graph, 15.minutes)

  it should "distinguish models with different tick durations" in:
    SimulationModel(graph, 15.minutes) should not be
      SimulationModel(graph, 1.hour)

  it should "distinguish models with different grid graphs" in:
    val otherGraph =
      GridGraph(
        nodes = List(externalGrid),
        cables = Nil
      )

    SimulationModel(graph, 15.minutes) should not be
      SimulationModel(otherGraph, 15.minutes)

  it should "produce an updated immutable value through copy" in:
    val original = SimulationModel(graph, 15.minutes)

    val updated = original.copy(delta = 30.minutes)

    original.delta shouldBe 15.minutes
    updated.delta shouldBe 30.minutes
    updated.grid shouldBe original.grid
