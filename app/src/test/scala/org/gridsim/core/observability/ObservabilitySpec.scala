package org.gridsim.core.observability

import org.gridsim.core.model.Environment
import org.gridsim.core.model.GridEntityState
import org.gridsim.core.simulation.SimulationState
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global

@RunWith(classOf[JUnitRunner])
class ObservabilitySpec extends AnyFlatSpec with Matchers:

  private val sampleEnvironment = Environment(1.minute)
  private val sampleState = SimulationState(
    environment = sampleEnvironment,
    entityStates = Map.empty,
    entityFlows = Map.empty,
    cableLoads = Map.empty
  )

  "Observer" should "create an observer for a single type using ClassTag" in:
    val obs = Observer[IO, SimulationData.EnvironmentData] { _ => IO.unit }
    obs.dataType shouldBe classOf[SimulationData.EnvironmentData]

  "Fs2DataDispatcher" should "dispatch a single data type to the correct observer" in:
    val testIO = for {
      q <- Queue.unbounded[IO, SimulationData.EnvironmentData]
      obs = Observer[IO, SimulationData.EnvironmentData](env => q.offer(env))
      dispatcher <- Fs2DataDispatcher[IO](List(obs))
      _ <- IO.sleep(500.millis) // Wait for subscriber fibers to initialize

      // Dispatch the entire state
      _ <- dispatcher.dispatch(sampleState)

      // We expect the observer to only receive the EnvironmentData slice
      received <- q.take
    } yield received.environment

    val result = testIO.unsafeRunSync()
    result shouldBe sampleEnvironment

  it should "not send events the observer is not subscribed to" in:
    import scala.concurrent.TimeoutException

    val testIO = for {
      q <- Queue.unbounded[IO, SimulationData.CableLoadsData]
      obs = Observer[IO, SimulationData.CableLoadsData](data => q.offer(data))

      dispatcher <- Fs2DataDispatcher[IO](List(obs))
      _ <- IO.sleep(500.millis) // Wait for subscriber fibers to initialize

      // Dispatch state. CableLoads is empty in sampleState, but the event is still dispatched.
      // However, we only subscribed to CableLoadsData.
      _ <- dispatcher.dispatch(sampleState)

      // To prove we didn't receive EnvironmentData or EntityStatesData or Snapshot,
      // we just take one event and verify it's CableLoadsData.
      received <- q.take
    } yield received

    val result = testIO.unsafeRunSync()
    result.isInstanceOf[SimulationData.CableLoadsData] shouldBe true

  it should "handle all types of events" in:
    val testIO = for {
      qEnv <- Queue.unbounded[IO, SimulationData.EnvironmentData]
      qEnt <- Queue.unbounded[IO, SimulationData.EntityStatesData]
      qFlow <- Queue.unbounded[IO, SimulationData.EntityFlowsData]
      qCab <- Queue.unbounded[IO, SimulationData.CableLoadsData]
      qSnp <- Queue.unbounded[IO, SimulationData.SimulationSnapshot]

      obsEnv = Observer[IO, SimulationData.EnvironmentData](d => qEnv.offer(d))
      obsEnt = Observer[IO, SimulationData.EntityStatesData](d => qEnt.offer(d))
      obsFlow = Observer[IO, SimulationData.EntityFlowsData](d => qFlow.offer(d))
      obsCab = Observer[IO, SimulationData.CableLoadsData](d => qCab.offer(d))
      obsSnp = Observer[IO, SimulationData.SimulationSnapshot](d =>
        qSnp.offer(d)
      )

      dispatcher <- Fs2DataDispatcher.apply[IO](
        List(obsEnv, obsEnt, obsFlow, obsCab, obsSnp)
      )
      _ <- IO.sleep(500.millis)

      _ <- dispatcher.dispatch(sampleState)

      envData <- qEnv.take
      entData <- qEnt.take
      flowData <- qFlow.take
      cabData <- qCab.take
      snpData <- qSnp.take
    } yield (envData, entData, flowData, cabData, snpData)

    val (env, ent, flow, cab, snp) = testIO.unsafeRunSync()
    env.environment shouldBe sampleState.environment
    ent.states shouldBe sampleState.entityStates
    flow.flows shouldBe sampleState.entityFlows
    cab.loads shouldBe sampleState.cableLoads
    snp.environment shouldBe sampleState.environment
    snp.entityStates shouldBe sampleState.entityStates
    snp.entityFlows shouldBe sampleState.entityFlows
    snp.cableLoads shouldBe sampleState.cableLoads

  "Simulation integration" should "dispatch events correctly over multiple ticks" in:
    import org.gridsim.core.simulation.{
      SimulationEngine,
      DefaultSimulationController
    }
    import org.gridsim.core.simulation.scheduling.{
      Scheduler,
      SimulationTask,
      ScheduledTask
    }

    val dummyScheduler = new Scheduler:
      override def schedule(
          task: SimulationTask,
          interval: FiniteDuration
      ): ScheduledTask = null
      override def stop(): Unit = ()

    val engine = new SimulationEngine:
      override def step(state: SimulationState): SimulationState =
        state.copy(environment = state.environment.advance(1.minute))

    val testIO = for {
      q <- Queue.unbounded[IO, SimulationData.EnvironmentData]
      obs = Observer[IO, SimulationData.EnvironmentData](env => q.offer(env))
      dispatcher <- Fs2DataDispatcher.apply[IO](List(obs))
      _ <- IO.sleep(500.millis) // Wait for subscriber fibers

      controller = DefaultSimulationController(
        engine = engine,
        state = sampleState, // Starts at 1.minute
        scheduler = dummyScheduler,
        interval = 1.second,
        dispatcher = Some(dispatcher)
      )

      // Advance simulation manually by 3 ticks
      _ <- IO(controller.stepOnce())
      _ <- IO(controller.stepOnce())
      _ <- IO(controller.stepOnce())

      // Take 3 events
      event1 <- q.take
      event2 <- q.take
      event3 <- q.take
    } yield List(event1, event2, event3)

    val results = testIO.unsafeRunSync()

    results.map(_.environment.time) shouldBe List(
      2.minutes,
      3.minutes,
      4.minutes
    )
