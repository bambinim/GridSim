package org.gridsim.core.validation

import cats.syntax.all.*
import org.gridsim.core.common.*
import org.gridsim.core.model.error.DomainError
import org.gridsim.core.model.error.DomainError.*
import org.gridsim.core.model.network.{
  Cable,
  CableConnections,
  ExternalGrid,
  GridGraph
}
import org.gridsim.core.model.{Environment, GridEntity, GridEntityState}
import org.gridsim.core.simulation.{
  SimulationModel,
  SimulationSetup,
  SimulationState
}
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class SimulationValidatorSpec extends AnyFlatSpec with Matchers:
  import SimulationValidator.given

  private final case class TestEntity(id: String) extends GridEntity
  private final case class TestEntityState(entityId: String)
      extends GridEntityState

  private val externalGrid = ExternalGrid("external-grid")
  private val house = TestEntity("house-1")
  private val houseState = TestEntityState(house.id)
  private val cable =
    Cable(
      CableConnections(externalGrid.id, house.id),
      10.kw
    )
  private val grid =
    GridGraph(
      nodes = List(externalGrid, house),
      cables = List(cable)
    )
  private val model = SimulationModel(grid, 15.minutes)
  private val state =
    SimulationState(
      environment = Environment(0.minutes),
      entityStates = Map(houseState.entityId -> houseState)
    )

  "SimulationValidator" should "accept a coherent simulation setup" in:
    val result = SimulationSetup.make(state, model)

    result.isValid shouldBe true

  it should "reject a non-positive simulation delta" in:
    val invalidModel = model.copy(delta = 0.minutes)

    val result = SimulationSetup.make(state, invalidModel)

    result.fold(
      errors =>
        errors.toList should contain(
          ValueMustBePositive("Simulation Delta", 0.0)
        ),
      _ => fail("It should have failed")
    )

  it should "reject an entity state whose map key does not match its entity id" in:
    val invalidState =
      state.copy(
        entityStates = Map("wrong-id" -> houseState)
      )

    val result = SimulationSetup.make(invalidState, model)

    result.fold(
      errors =>
        errors.toList should contain(
          EntityStateKeyMismatch("wrong-id", house.id)
        ),
      _ => fail("It should have failed")
    )

  it should "reject an entity state that has no matching model node" in:
    val orphanState = TestEntityState("orphan-1")
    val invalidState =
      state.copy(
        entityStates = Map(orphanState.entityId -> orphanState)
      )

    val result = SimulationSetup.make(invalidState, model)

    result.fold(
      errors =>
        errors.toList should contain(
          EntityStateWithoutModel(orphanState.entityId)
        ),
      _ => fail("It should have failed")
    )

  it should "reject entity flows that reference unknown model nodes" in:
    val invalidState =
      state.copy(
        entityFlows = Map("missing-node" -> Flow.Balanced)
      )

    val result = SimulationSetup.make(invalidState, model)

    result.fold(
      errors =>
        errors.toList should contain(EntityFlowWithoutModel("missing-node")),
      _ => fail("It should have failed")
    )

  it should "reject cable loads that reference cables outside the model" in:
    val unknownCable =
      Cable(
        CableConnections("house-1", "unknown-node"),
        5.kw
      )
    val invalidState =
      state.copy(
        cableLoads = Map(unknownCable -> 1.kwh)
      )

    val result = SimulationSetup.make(invalidState, model)

    result.fold(
      errors =>
        errors.toList should contain(
          CableLoadWithoutCable("house-1", "unknown-node")
        ),
      _ => fail("It should have failed")
    )

  it should "accumulate multiple coherence errors" in:
    val invalidState =
      state.copy(
        entityStates = Map("wrong-id" -> houseState),
        entityFlows = Map("missing-flow-node" -> Flow.Balanced),
        cableLoads = Map(Cable(CableConnections("x", "y"), 1.kw) -> 1.kwh)
      )

    val result =
      SimulationSetup.make(invalidState, model.copy(delta = 0.minutes))

    result.fold(
      errors =>
        val errorsList = errors.toList

        errorsList should contain(ValueMustBePositive("Simulation Delta", 0.0))
        errorsList should contain(EntityStateKeyMismatch("wrong-id", house.id))
        errorsList should contain(EntityStateWithoutModel("wrong-id"))
        errorsList should contain(EntityFlowWithoutModel("missing-flow-node"))
        errorsList should contain(CableLoadWithoutCable("x", "y"))
      ,
      _ => fail("It should have failed")
    )
