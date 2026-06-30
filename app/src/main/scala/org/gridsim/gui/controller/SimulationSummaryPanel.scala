package org.gridsim.gui.controller

import org.gridsim.core.observability.SimulationData
import org.gridsim.core.observability.SimulationData.SimulationSnapshot
import org.gridsim.core.simulation.{SimulationControllerState, SimulationModel, SimulationState}
import org.gridsim.gui.view.SimulationSummaryView
import scalafx.scene.Parent

final class SimulationSummaryPanel (model: SimulationModel) extends SimulationPanel:
  private val controller = SimulationSummaryController(model)
  private val view = SimulationSummaryView()
  
  override def root: Parent = view.root

  override def renderCurrent(
    state: SimulationState, 
    controllerState: SimulationControllerState
  ): Unit =
    view.render(controller.onState(state, controllerState))

  override def renderSnapshot(
    snapshot: SimulationSnapshot, 
    controllerState: SimulationControllerState
  ): Unit =
    view.render(controller.onSnapshot(snapshot, controllerState))
    
    
