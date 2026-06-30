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

object SimulationFactory:
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
