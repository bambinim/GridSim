package org.gridsim.gui.runtime

import org.gridsim.core.simulation.{DefaultSimulationController, DefaultSimulationEngine, SimulationModel, SimulationState}
import org.gridsim.core.solver.SimplePowerFlowSolver
import org.gridsim.core.simulation.scheduling.DefaultScheduler
import org.gridsim.gui.model.RunningSimulation

import scala.concurrent.duration.DurationInt

object SimulationFactory:
  def createSimpleSimulation(model: SimulationModel, state: SimulationState): RunningSimulation =
    val engine = DefaultSimulationEngine(model, SimplePowerFlowSolver(model.grid))
    val controller = DefaultSimulationController(engine, state, DefaultScheduler(), 2.seconds)

    RunningSimulation(model, controller)
