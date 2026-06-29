package org.gridsim.gui.view

import org.gridsim.core.simulation.SimulationSnapshot
import org.gridsim.gui.controller.SimulationGuiController
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.control.Button

/**
 * Main view for the active simulation dashboard.
 * It observes state changes through the SimulationGuiController.
 */
class SimulationView(val controller: SimulationGuiController) extends BorderPane with ViewFX:
  override def root: Parent = this

  private val titleLabel = new Label("Simulazione Micro-Grid")
  private val simulationStateLabel = new Label(s"Simulation State: ${controller.controllerState}")
  private val stateLabel = new Label(s"Tempo: ${controller.currentState.environment.time.toMinutes}m")

  private val playButton = new Button("Play/Pause"):
    defaultButton = true
    styleClass += "primary-button"
    onAction = _ => togglePlayButton()

  // Layout Setup
  top = new VBox(8):
    styleClass += "simulation-header"
    children = Seq(titleLabel, simulationStateLabel, stateLabel, playButton)

  controller.setOnChanged(updateUi)

  private def updateUi(snapshot: SimulationSnapshot): Unit =
    simulationStateLabel.text = s"Simulation State: ${snapshot.controllerState}"
    stateLabel.text =
      s"Tempo simulato: ${snapshot.state.environment.time.toMinutes} minuti"

  private def togglePlayButton(): Unit =
    controller.togglePlayPause()
