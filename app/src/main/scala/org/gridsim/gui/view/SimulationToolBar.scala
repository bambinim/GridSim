package org.gridsim.gui.view

import org.gridsim.gui.model.SimulationDashboardState
import scalafx.scene.Parent
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.HBox

class SimulationToolBar(
  onTogglePlayPause: () => Unit,
  onStep: () => Unit,
  onStop: () => Unit                     
) extends HBox with ViewFX:
  override def root: Parent = this

  private val startButton = new Button("Play"):
    onAction = _ => onTogglePlayPause()
    
  private val stopButton = new Button("Stop"):
    onAction = _ => onStop()
    
  private val simulatedTimeLabel = 
    new Label("Simulated Time")
    
  private val simulationStatus =
    new Label("Simulation Status")

  children = Seq(
    simulationStatus,
    simulatedTimeLabel,
    startButton,
    stopButton
  )
  
  def render(state: SimulationDashboardState): Unit =
    simulatedTimeLabel.text = state.simulatedMinutes.toString
    simulationStatus.text = state.controllerState.toString