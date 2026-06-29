package org.gridsim.gui.model

import org.gridsim.core.simulation.{SimulationController, SimulationModel}

case class RunningSimulation(
  model: SimulationModel,
  controller: SimulationController
)
