package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.SimulationControlViewModel
import scalafx.geometry.Pos
import scalafx.scene.Parent
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.HBox

/**
 * View panel for controlling the simulation (Play/Pause, Step, Stop, Exit).
 * Binds button actions and disable states to SimulationControlViewModel.
 *
 * @param viewModel the viewmodel driving this simulation control view
 */
class SimulationControlView(viewModel: SimulationControlViewModel) extends HBox(16) with ViewFX:
  /**
   * The root parent component of this view.
   */
  override def root: Parent = this

  alignment = Pos.Center
  styleClass += "control-panel"

  private val statusPrefix = new Label("SIMULATION STATUS:"):
    styleClass += "section-label"

  private val statusLabel = new Label:
    text <== viewModel.statusText
    styleClass += "status-badge"

  private val playPauseButton = new Button:
    text <== viewModel.playPauseText
    disable <== viewModel.playPauseDisabled
    styleClass ++= Seq("control-button", "play-pause-btn")
    onAction = _ => viewModel.togglePlayPause()

  private val stepButton = new Button("Step"):
    disable <== viewModel.stepDisabled
    styleClass ++= Seq("control-button", "step-btn")
    onAction = _ => viewModel.stepOnce()

  private val exitButton = new Button("Exit"):
    disable <== viewModel.exitDisabled
    styleClass ++= Seq("control-button", "exit-btn")
    onAction = _ => viewModel.exit()

  // Dynamically update style classes of the status badge on state change
  private def updateBadgeStyle(status: String): Unit =
    statusLabel.styleClass.removeAll("status-running", "status-paused")
    status match
      case "RUNNING" => statusLabel.styleClass += "status-running"
      case "PAUSED"  => statusLabel.styleClass += "status-paused"
      case _         => ()

  viewModel.statusText.onChange { (_, _, newStatus) =>
    updateBadgeStyle(newStatus)
  }

  updateBadgeStyle(viewModel.statusText.value)

  children = Seq(
    statusPrefix,
    statusLabel,
    new Label(" | "):
      styleClass += "muted-text"
    ,
    playPauseButton,
    stepButton,
    exitButton
  )
