package org.gridsim.gui.runtime

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.gridsim.core.observability.{Observer, SimulationData}
import org.gridsim.core.simulation.{SimulationControllerFactory, SimulationModel, SimulationState}
import org.gridsim.gui.model.RunningSimulation
import fs2.concurrent.SignallingRef
import scala.concurrent.duration.DurationInt
import org.gridsim.core.statistics.{SimulationStatistics, StatisticsCollector}

/**
 * Factory for instantiating and configuring running simulation loops.
 *
 * Handles creation of underlying snapshot queues, GUI observers, event dispatchers,
 * simulation engine solver bindings, and wrapping them in [[RunningSimulation]].
 */
object RunningSimulationFactory:
  /**
   * Constructs and wires a standard simulation control loop from a model and state.
   *
   * Creates an FS2-stream-based pipeline to dispatch updates to observers.
   *
   * @param model the grid topology and configuration of the simulation
   * @param state the initial conditions of the grid entities and environment
   * @return the wired [[RunningSimulation]] ready for play/pause/step controls
   */
  def createSimpleSimulation(
      model: SimulationModel,
      state: SimulationState
  ): RunningSimulation =
    val initialSnapshot: SimulationData.SimulationSnapshot = SimulationData.SimulationSnapshot(
      state.environment,
      state.entityStates,
      state.entityFlows,
      state.cableLoads
    )
    val snapshotSignal = SignallingRef[IO, SimulationData.SimulationSnapshot](initialSnapshot).unsafeRunSync()
    val guiObserver = Observer[IO, SimulationData.SimulationSnapshot](snapshotSignal.set)

    val statisticsSignal = SignallingRef[IO, SimulationStatistics](SimulationStatistics.empty).unsafeRunSync()
    val statisticsObserver = Observer[IO, SimulationData.SimulationSnapshot] { snapshot =>
      import cats.syntax.monoid.catsSyntaxSemigroup
      statisticsSignal.update(_ |+| StatisticsCollector.collect(snapshot))
    }

    val controller = SimulationControllerFactory.create(
      model,
      state,
      observers = List(guiObserver, statisticsObserver),
      tickInterval = 2.seconds
    )

    RunningSimulation(model, controller, snapshotSignal, statisticsSignal)
