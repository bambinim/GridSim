package org.gridsim.gui.runtime

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import fs2.Stream as Fs2Stream
import org.gridsim.core.observability.{Fs2DataDispatcher, Observer, SimulationData}
import org.gridsim.core.simulation.{DefaultSimulationController, DefaultSimulationEngine, SimulationModel, SimulationState}
import org.gridsim.core.solver.SimplePowerFlowSolver
import org.gridsim.core.simulation.scheduling.DefaultScheduler
import org.gridsim.gui.model.RunningSimulation

import scala.concurrent.duration.DurationInt

/**
 * Factory for instantiating and configuring running simulation loops.
 *
 * Handles creation of underlying snapshot queues, GUI observers, event dispatchers,
 * simulation engine solver bindings, and wrapping them in [[RunningSimulation]].
 */
object SimulationFactory:
  /**
   * Constructs and wires a standard simulation control loop from a model and state.
   *
   * Creates an FS2-stream-based pipeline to dispatch updates to observers.
   *
   * @param model the grid topology and configuration of the simulation
   * @param state the initial conditions of the grid entities and environment
   * @return the wired [[RunningSimulation]] ready for play/pause/step controls
   */
  def createSimpleSimulation(model: SimulationModel, state: SimulationState): RunningSimulation =
    val snapshotQueue =
      Queue.unbounded[IO, SimulationData.SimulationSnapshot].unsafeRunSync()
    val guiObserver =
      Observer[IO, SimulationData.SimulationSnapshot](snapshotQueue.offer)
    val dispatcher =
      Fs2DataDispatcher[IO](List(guiObserver)).unsafeRunSync()
    val engine = DefaultSimulationEngine(model, SimplePowerFlowSolver(model.grid))
    val controller = DefaultSimulationController(
      engine,
      state,
      DefaultScheduler(),
      2.seconds,
      dispatcher = Some(dispatcher)
    )

    RunningSimulation(
      model,
      controller,
      Fs2Stream.fromQueueUnterminated(snapshotQueue)
    )
