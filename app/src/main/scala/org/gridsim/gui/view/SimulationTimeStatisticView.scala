package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.SimulationTimeStatisticViewModel
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

/**
 * View panel showing total simulated elapsed time and tick count.
 *
 * @param viewModel the viewmodel driving this panel
 */
class SimulationTimeStatisticView(viewModel: SimulationTimeStatisticViewModel) extends VBox with ViewFX:
  override def root: Parent = this

  styleClass += "statistics"

  private val tickLabel =
    new Label():
      styleClass += "title"
      text <== viewModel.tickText

  private val initialTimeLabel =
    new Label():
      text <== viewModel.initialTimeText

  private val elapsedTimeLabel =
    new Label():
      text <== viewModel.elapsedTimeText

  children = Seq(
    tickLabel,
    initialTimeLabel,
    elapsedTimeLabel
  )
