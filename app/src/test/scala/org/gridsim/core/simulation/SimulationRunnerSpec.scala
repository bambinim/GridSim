package org.gridsim.core.simulation

import org.gridsim.core.model.Environment
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration.*

@RunWith(classOf[JUnitRunner])
class SimulationRunnerSpec extends AnyFlatSpec with Matchers:

  private class CountingEngine extends SimulationEngine:

    var calls = 0

    override def step(state: SimulationState): SimulationState =
      calls = calls + 1
      state.copy(environment = state.environment.advance(1.minute))

  private def initialState: SimulationState =
    SimulationState(
      environment = Environment(0.minutes),
      entityStates = Nil
    )

  "DefaultSimulationRunner" should "expose the initial state before running" in:
    val engine = CountingEngine()
    val runner = DefaultSimulationRunner(engine, initialState, 50.millis)

    runner.currentState shouldBe initialState
    engine.calls shouldBe 0

  it should "advance the current state when stepped manually" in:
    val engine = CountingEngine()
    val runner = DefaultSimulationRunner(engine, initialState, 50.millis)

    val next = runner.stepOnce()

    next.environment.time shouldBe 1.minute
    runner.currentState shouldBe next
    engine.calls shouldBe 1

  it should "advance from the latest state on every manual step" in:
    val engine = CountingEngine()
    val runner = DefaultSimulationRunner(engine, initialState, 50.millis)

    runner.stepOnce()
    val second = runner.stepOnce()

    second.environment.time shouldBe 2.minutes
    runner.currentState.environment.time shouldBe 2.minutes
    engine.calls shouldBe 2

  it should "start a periodic simulation loop" in:
    val engine = CountingEngine()
    val runner = DefaultSimulationRunner(engine, initialState, 20.millis)

    runner.start()
    Thread.sleep(80)
    runner.stop()

    runner.currentState.environment.time.toMinutes should be >= 2L
    engine.calls should be >= 2

  it should "stop the periodic simulation loop" in:
    val engine = CountingEngine()
    val runner = DefaultSimulationRunner(engine, initialState, 20.millis)

    runner.start()
    Thread.sleep(80)
    runner.stop()

    val callsAfterStop = engine.calls
    val stateAfterStop = runner.currentState

    Thread.sleep(100)

    engine.calls shouldBe callsAfterStop
    runner.currentState shouldBe stateAfterStop

  it should "not create more than one active loop when started repeatedly" in:
    val engine = CountingEngine()
    val runner = DefaultSimulationRunner(engine, initialState, 50.millis)

    runner.start()
    runner.start()
    runner.start()

    Thread.sleep(120)
    runner.stop()

    val callsAfterStop = engine.calls
    Thread.sleep(120)

    engine.calls shouldBe callsAfterStop

  it should "resume the simulation after it has been paused" in:
    val engine = CountingEngine()
    val runner = DefaultSimulationRunner(engine, initialState, 20.millis)
  
    runner.start()
    Thread.sleep(80)
    runner.pause()
  
    val stateAfterPause = runner.currentState
  
    Thread.sleep(80)
    runner.currentState shouldBe stateAfterPause
  
    runner.resume()
    Thread.sleep(80)
    runner.stop()
  
    runner.currentState.environment.time should be > stateAfterPause.environment.time
