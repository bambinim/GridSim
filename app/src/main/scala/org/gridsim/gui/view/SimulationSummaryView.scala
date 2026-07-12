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
class SimulationSummaryView(name: String, viewModel: SimulationSummaryViewModel) extends VBox with ViewFX:
  override def root: Parent = this

  styleClass += "summary"

  private val nameLabel =
    new Label():
      styleClass += "title"
      text = name

  private val numEntities =
    new Label():
      text <== viewModel.entitiesText

  private val numCables =
    new Label():
      text <== viewModel.cablesText

  children = Seq(
    nameLabel,
    numEntities,
    numCables
  )
