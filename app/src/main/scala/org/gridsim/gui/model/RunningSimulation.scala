package org.gridsim.gui.model

import cats.effect.IO
import fs2.Stream as Fs2Stream
import org.gridsim.core.observability.SimulationData
import org.gridsim.core.simulation.{SimulationController, SimulationModel}

case class RunningSimulation(
  model: SimulationModel,
  controller: SimulationController,
  snapshotEvents: Fs2Stream[IO, SimulationData.SimulationSnapshot]
)
