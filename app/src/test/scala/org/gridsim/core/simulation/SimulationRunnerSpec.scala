package org.gridsim.core.simulation

import org.gridsim.core.model.Environment
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

import org.gridsim.core.simulation.scheduling.{ScheduledTask, Scheduler, SimulationTask}

@RunWith(classOf[JUnitRunner])
class SimulationRunnerSpec extends AnyFlatSpec with Matchers:

  private class CountingEngine extends SimulationEngine:

    var calls = 0

    override def step(state: SimulationState): SimulationState =
      calls = calls + 1
      state.copy(environment = state.environment.advance(1.minute))

  private class ManualScheduledTask(task: SimulationTask) extends ScheduledTask:
    private var cancelled = false

    def run(): Unit =
      if !cancelled then task()

    override def cancel(): Unit =
      cancelled = true

    def isActive: Boolean = !cancelled

  private class ManualScheduler extends Scheduler:
    private var tasks = List.empty[ManualScheduledTask]
    private var stopped = false

    override def schedule(task: SimulationTask, interval: FiniteDuration): ScheduledTask =
      if stopped then throw IllegalStateException("Scheduler has been stopped")
      val scheduledTask = ManualScheduledTask(task)
      tasks = scheduledTask :: tasks
      scheduledTask

    override def stop(): Unit =
      stopped = true
      tasks.foreach(_.cancel())

    def tick(): Unit =
      tasks.reverse.foreach(_.run())

    def activeTaskCount: Int =
      tasks.count(_.isActive)

  private def initialState: SimulationState =
    SimulationState(
      environment = Environment(0.minutes),
      entityStates = Nil
    )

  "DefaultSimulationRunner" should "expose the initial state before running" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val runner = DefaultSimulationRunner(engine, initialState, scheduler, 50.millis)

    runner.currentState shouldBe initialState
    engine.calls shouldBe 0

  it should "advance the current state when stepped manually" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val runner = DefaultSimulationRunner(engine, initialState, scheduler, 50.millis)

    val next = runner.stepOnce()

    next.environment.time shouldBe 1.minute
    runner.currentState shouldBe next
    engine.calls shouldBe 1

  it should "advance from the latest state on every manual step" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val runner = DefaultSimulationRunner(engine, initialState, scheduler, 50.millis)

    runner.stepOnce()
    val second = runner.stepOnce()

    second.environment.time shouldBe 2.minutes
    runner.currentState.environment.time shouldBe 2.minutes
    engine.calls shouldBe 2

  it should "start a periodic simulation loop" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val runner = DefaultSimulationRunner(engine, initialState, scheduler, 20.millis)

    runner.start()
    scheduler.tick()
    scheduler.tick()
    runner.stop()

    runner.currentState.environment.time.toMinutes shouldBe 2L
    engine.calls shouldBe 2

  it should "stop the periodic simulation loop" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val runner = DefaultSimulationRunner(engine, initialState, scheduler, 20.millis)

    runner.start()
    scheduler.tick()
    runner.stop()

    val callsAfterStop = engine.calls
    val stateAfterStop = runner.currentState

    scheduler.tick()

    engine.calls shouldBe callsAfterStop
    runner.currentState shouldBe stateAfterStop

  it should "not create more than one active loop when started repeatedly" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val runner = DefaultSimulationRunner(engine, initialState, scheduler, 50.millis)

    runner.start()
    runner.start()
    runner.start()

    scheduler.activeTaskCount shouldBe 1
    scheduler.tick()
    runner.stop()

    val callsAfterStop = engine.calls
    scheduler.tick()

    engine.calls shouldBe callsAfterStop

  it should "resume the simulation after it has been paused" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val runner = DefaultSimulationRunner(engine, initialState, scheduler, 20.millis)

    runner.start()
    scheduler.tick()
    runner.pause()

    val stateAfterPause = runner.currentState

    scheduler.tick()
    runner.currentState shouldBe stateAfterPause

    runner.resume()
    scheduler.activeTaskCount shouldBe 1
    scheduler.tick()
    runner.stop()

    runner.currentState.environment.time should be > stateAfterPause.environment.time
