package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.FlowStatisticViewModel
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
class FlowStatisticView(viewModel: FlowStatisticViewModel) extends VBox with ViewFX:
  override def root: Parent = this

  styleClass += "statistics"

  private val currentNetFlow =
    new Label():
      styleClass += "title"
      text <== viewModel.currentNetFlow

  private val avgNetFlowLabel =
    new Label():
      text <== viewModel.avgNetFlowText

  private val exportedLabel =
    new Label():
      text <== viewModel.exportedText

  private val peakExportedLabel =
    new Label():
      text <== viewModel.peakExportText

  private val importedLabel =
    new Label():
      text <== viewModel.importedText

  private val peakImportedLabel =
    new Label():
      text <== viewModel.peakImportText

  children = Seq(
    currentNetFlow,
    avgNetFlowLabel,
    exportedLabel,
    peakExportedLabel,
    importedLabel,
    peakImportedLabel
  )
