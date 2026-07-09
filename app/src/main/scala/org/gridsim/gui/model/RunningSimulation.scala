package org.gridsim.gui.model

import cats.effect.IO
import fs2.concurrent.SignallingRef
import org.gridsim.core.observability.SimulationData
import org.gridsim.core.simulation.{SimulationController, SimulationModel}
import org.gridsim.core.statistics.{NetFlowHistory, SimulationStatistics}

/**
 * Representation of an active simulation loop setup.
 *
 * Combines the domain model topology, the execution controller, and the stream of updates.
 *
 * @param model the static topology and parameters configuration of the grid
 * @param controller the engine state controller (handling start, pause, resume, step)
 * @param snapshotSignal signaling stream emitting simulation snapshot updates
 */
case class RunningSimulation(
  model: SimulationModel,
  controller: SimulationController,
  snapshotSignal: SignallingRef[IO, SimulationData.SimulationSnapshot],
  statisticsSignal: SignallingRef[IO, SimulationStatistics],
  historySignal: SignallingRef[IO, NetFlowHistory]
)
