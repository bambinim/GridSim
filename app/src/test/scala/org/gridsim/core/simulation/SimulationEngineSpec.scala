package org.gridsim.core.simulation

import org.gridsim.core.behaviour.{EntityEvolutionDispatcher, EvolutionRequest}
import org.gridsim.core.common.{Energy, Flow, kw, kwh}
import org.gridsim.core.model.{Environment, GridEntity, GridEntityState}
import org.gridsim.core.model.network.{Cable, CableConnections, ExternalGrid, GridGraph}
import org.gridsim.core.solver.PowerFlowSolver
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class SimulationEngineSpec extends AnyFlatSpec with Matchers:

  // Mock domain entities for unit testing
  private case class TestEntity(id: String) extends GridEntity
  private case class TestEntityState(entityId: String, value: String = "") extends GridEntityState

  // Helper/Mock definitions
  private val defaultGraph = GridGraph(List(ExternalGrid("external-grid")), Nil)
  private val defaultModel = SimulationModel(defaultGraph)

  private def createEngine(
    model: SimulationModel = defaultModel,
    flowSolver: PowerFlowSolver = mockFlowSolver
  )(using dispatcher: EntityEvolutionDispatcher): DefaultSimulationEngine =
    DefaultSimulationEngine(model, flowSolver)

  private val mockFlowSolver = new PowerFlowSolver:
    override def solve(entityFlowMap: scala.collection.Map[String, Flow[Energy]]): Map[Cable, Energy] =
      Map.empty

  private given mockDispatcher: EntityEvolutionDispatcher = new EntityEvolutionDispatcher:
    override def evolve(
      request: EvolutionRequest
    ): (GridEntityState, Flow[Energy]) =
      (request.state, Flow.Balanced)

  "SimulationEngine" should "advance the environment by the model delta" in:
    val current =
      SimulationState(
        environment = Environment(2.hours),
        entityStates = Map.empty
      )

    val engine = createEngine()
    val next = engine.step(current, 15.minutes)

    next.environment.time shouldBe 2.hours + 15.minutes

  it should "resolve every grid entity state using the dispatcher" in:
    val entity = TestEntity("entity-1")
    val state = TestEntityState("entity-1", "initial")
    val graph = GridGraph(
      nodes = List(ExternalGrid("external-grid"), entity),
      cables = Nil
    )
    val model = SimulationModel(graph)

    val expectedNextState = TestEntityState("entity-1", "evolved")
    val expectedFlow = Flow.Surplus(Energy(0.5))

    var calledWithArgs: Option[(GridEntityState, GridEntity, Environment, FiniteDuration)] = None
    given localDispatcher: EntityEvolutionDispatcher = new EntityEvolutionDispatcher:
      override def evolve(
        request: EvolutionRequest
      ): (GridEntityState, Flow[Energy]) =
        calledWithArgs = Some((request.state, request.entity, request.env, request.delta))
        (expectedNextState, expectedFlow)

    val engine = createEngine(model = model)
    val current =
      SimulationState(
        environment = Environment(2.hours),
        entityStates = Map(state.entityId -> state)
      )

    val next = engine.step(current, 15.minutes)

    // Verify dispatcher called with advanced environment and model delta
    val expectedAdvancedEnv = current.environment.advance(15.minutes)
    calledWithArgs shouldBe Some((state, entity, expectedAdvancedEnv, 15.minutes))

    // Verify next state maps the updated states and flows
    next.entityStates.get("entity-1") shouldBe Some(expectedNextState)
    next.entityFlows.get("entity-1") shouldBe Some(expectedFlow)

  it should "fail to step if a state has no matching model" in:
    val state = TestEntityState("unmatched-entity")
    val current =
      SimulationState(
        environment = Environment(2.hours),
        entityStates = Map(state.entityId -> state)
      )

    val engine = createEngine()

    an[IllegalArgumentException] should be thrownBy engine.step(current, 15.minutes)

  it should "calculate the load on every cable using the flow solver" in:
    // Setup solver that returns a pre-configured load
    val dummyCable = Cable(
      CableConnections("node-1", "node-2"),
      maxCapacity = 10.kw
    )
    val expectedLoads = Map(dummyCable -> Energy(1.5))

    val entity = TestEntity("node-1")
    val state = TestEntityState("node-1")
    val graph = GridGraph(
      nodes = List(entity),
      cables = List(dummyCable)
    )
    val model = SimulationModel(graph)

    var solverReceivedFlows: Option[Map[String, Flow[Energy]]] = None
    val localSolver = new PowerFlowSolver:
      override def solve(entityFlowMap: scala.collection.Map[String, Flow[Energy]]): Map[Cable, Energy] =
        solverReceivedFlows = Some(entityFlowMap.toMap)
        expectedLoads

    val engine = createEngine(model = model, flowSolver = localSolver)
    val current =
      SimulationState(
        environment = Environment(2.hours),
        entityStates = Map(state.entityId -> state)
      )

    val next = engine.step(current, 15.minutes)

    solverReceivedFlows shouldBe Some(Map("node-1" -> Flow.Balanced))

    next.cableLoads shouldBe expectedLoads
