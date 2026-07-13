package org.gridsim.core.simulation

import org.gridsim.core.model.Environment
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

import org.gridsim.core.simulation.scheduling.{ScheduledTask, Scheduler, SimulationTask}

@RunWith(classOf[JUnitRunner])
class SimulationControllerSpec extends AnyFlatSpec with Matchers:

  private class CountingEngine extends SimulationEngine:

    var calls = 0
    var receivedDeltas = List.empty[FiniteDuration]

    override def step(state: SimulationState, delta: FiniteDuration): SimulationState =
      calls = calls + 1
      receivedDeltas = receivedDeltas :+ delta
      state.copy(environment = state.environment.advance(1.minute))

  private class ManualScheduledTask(task: SimulationTask) extends ScheduledTask:
    private var cancelled = false
    private var completed = false

    def run(): Unit =
      if isActive then
        completed = true
        task()

    override def cancel(): Unit =
      cancelled = true

    def isActive: Boolean = !cancelled && !completed

  private class ManualScheduler extends Scheduler:
    private var tasks = List.empty[ManualScheduledTask]
    private var delays = List.empty[FiniteDuration]
    private var stopped = false

    override def scheduleOnce(task: SimulationTask, delay: FiniteDuration): ScheduledTask =
      if stopped then throw IllegalStateException("Scheduler has been stopped")
      val scheduledTask = ManualScheduledTask(task)
      tasks = scheduledTask :: tasks
      delays = delay :: delays
      scheduledTask

    override def stop(): Unit =
      stopped = true
      tasks.foreach(_.cancel())

    def tick(): Unit =
      tasks.reverse.find(_.isActive).foreach(_.run())

    def activeTaskCount: Int =
      tasks.count(_.isActive)

    def scheduledDelays: List[FiniteDuration] =
      delays.reverse

  private def initialState: SimulationState =
    SimulationState(
      environment = Environment(0.minutes),
      entityStates = Map.empty
    )

  "DefaultSimulationRunner" should "expose the initial state before running" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val controller = DefaultSimulationController(engine, initialState, scheduler, SimulationConf(15.minutes))

    controller.currentState shouldBe initialState
    engine.calls shouldBe 0

  it should "advance the current state when stepped manually" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val controller = DefaultSimulationController(engine, initialState, scheduler, SimulationConf(15.minutes))

    val next = controller.stepOnce()

    next.environment.time shouldBe 1.minute
    controller.currentState shouldBe next
    engine.calls shouldBe 1

  it should "advance from the latest state on every manual step" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val controller = DefaultSimulationController(engine, initialState, scheduler, SimulationConf(15.minutes))

    controller.stepOnce()
    val second = controller.stepOnce()

    second.environment.time shouldBe 2.minutes
    controller.currentState.environment.time shouldBe 2.minutes
    engine.calls shouldBe 2

  it should "start a periodic simulation loop" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val controller = DefaultSimulationController(engine, initialState, scheduler, SimulationConf(15.minutes))

    controller.start()
    scheduler.tick()
    scheduler.tick()
    controller.stop()

    controller.currentState.environment.time.toMinutes shouldBe 2L
    engine.calls shouldBe 2

  it should "stop the periodic simulation loop" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val controller = DefaultSimulationController(engine, initialState, scheduler, SimulationConf(15.minutes))

    controller.start()
    scheduler.tick()
    controller.stop()

    val callsAfterStop = engine.calls
    val stateAfterStop = controller.currentState

    scheduler.tick()

    engine.calls shouldBe callsAfterStop
    controller.currentState shouldBe stateAfterStop

  it should "not create more than one active loop when started repeatedly" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val controller = DefaultSimulationController(engine, initialState, scheduler, SimulationConf(15.minutes))

    controller.start()
    controller.start()
    controller.start()

    scheduler.activeTaskCount shouldBe 1
    scheduler.tick()
    controller.stop()

    val callsAfterStop = engine.calls
    scheduler.tick()

    engine.calls shouldBe callsAfterStop

  it should "resume the simulation after it has been paused" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val controller = DefaultSimulationController(engine, initialState, scheduler, SimulationConf(15.minutes))

    controller.start()
    scheduler.tick()
    controller.pause()

    val stateAfterPause = controller.currentState

    scheduler.tick()
    controller.currentState shouldBe stateAfterPause

    controller.resume()
    scheduler.activeTaskCount shouldBe 1
    scheduler.tick()
    controller.stop()

    controller.currentState.environment.time should be > stateAfterPause.environment.time

  it should "update the simulation step delta via setTick" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val controller = DefaultSimulationController(engine, initialState, scheduler, SimulationConf(15.minutes))

    controller.configuration.delta shouldBe 15.minutes
    controller.setTick(1.hour)
    controller.configuration.delta shouldBe 1.hour

    controller.stepOnce()
    engine.receivedDeltas.last shouldBe 1.hour

  it should "apply a new speed when scheduling the tick after the pending one" in:
    val engine = CountingEngine()
    val scheduler = ManualScheduler()
    val controller = DefaultSimulationController(engine, initialState, scheduler, SimulationConf(15.minutes))

    controller.start()
    scheduler.scheduledDelays shouldBe List(1.second)

    controller.setSpeed(SimulationSpeed.UltraSpeed)

    engine.calls shouldBe 0
    scheduler.activeTaskCount shouldBe 1
    scheduler.scheduledDelays shouldBe List(1.second)

    scheduler.tick()

    engine.calls shouldBe 1
    scheduler.activeTaskCount shouldBe 1
    scheduler.scheduledDelays shouldBe List(1.second, 100.milliseconds)

    controller.stop()
