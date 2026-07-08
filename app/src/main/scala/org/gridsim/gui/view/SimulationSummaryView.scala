package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.SimulationSummaryViewModel
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

/**
 * View panel for displaying overall simulation metrics.
 *
 * This panel shows net energy flow (exporting/importing/balanced status),
 * active entity count, cable count, and current hour of the simulation.
 *
 * @param viewModel the viewmodel driving this simulation summary view
 */
class SimulationSummaryView(viewModel: SimulationSummaryViewModel) extends VBox with ViewFX:
  /**
   * The root parent component of this view.
   */
  override def root: Parent = this

  private val netFlowLabel =
    new Label():
      styleClass += "title"
      text <== viewModel.netFlowText

  private val numEntities =
    new Label():
      text <== viewModel.entitiesText

  private val numCables =
    new Label():
      text <== viewModel.cablesText

  private val simHours =
    new Label():
      text <== viewModel.timeText

  children = Seq(
    netFlowLabel,
    numEntities,
    numCables,
    simHours
  )
