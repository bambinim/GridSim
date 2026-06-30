package org.gridsim.gui.controller

import org.gridsim.core.observability.SimulationData.SimulationSnapshot
import org.gridsim.core.simulation.{SimulationControllerState, SimulationState}
import scalafx.scene.Parent

trait SimulationPanel:
  def root: Parent
  def renderCurrent(
    state: SimulationState,
    controller: SimulationControllerState               
  ): Unit
  def renderSnapshot(
    snapshot: SimulationSnapshot,
    controllerState: SimulationControllerState                
  ): Unit
