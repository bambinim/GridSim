package org.gridsim.gui.runtime

import org.gridsim.core.simulation.{DefaultSimulationController, DefaultSimulationEngine, SimulationController, SimulationModel, SimulationState}
import org.gridsim.core.solver.SimplePowerFlowSolver
import org.gridsim.core.behaviour.EntityEvolutionDispatcher.default
import org.gridsim.core.simulation.scheduling.DefaultScheduler

import scala.concurrent.duration.DurationInt

object SimulationFactory:
  def createSimpleSimulation(model: SimulationModel, state: SimulationState): SimulationController =
    val engine = DefaultSimulationEngine(model, SimplePowerFlowSolver(model.grid))
    
    DefaultSimulationController(engine, state, DefaultScheduler(), 2.seconds)
