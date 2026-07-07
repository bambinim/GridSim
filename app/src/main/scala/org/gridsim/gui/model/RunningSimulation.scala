package org.gridsim.gui.model

import cats.effect.IO
import fs2.Stream as Fs2Stream
import org.gridsim.core.observability.SimulationData
import org.gridsim.core.simulation.{SimulationController, SimulationModel}

/**
 * Representation of an active simulation loop setup.
 *
 * Combines the domain model topology, the execution controller, and the stream of updates.
 *
 * @param model the static topology and parameters configuration of the grid
 * @param controller the engine state controller (handling start, pause, resume, step)
 * @param snapshotEvents downstream FS2 stream emitting simulation snapshot updates
 */
case class RunningSimulation(
  model: SimulationModel,
  controller: SimulationController,
  snapshotEvents: Fs2Stream[IO, SimulationData.SimulationSnapshot]
)
