package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.StatisticsViewModel
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
class StatisticsView(viewModel: StatisticsViewModel) extends VBox with ViewFX:
  override def root: Parent = this

  styleClass += "statistics"

  private val avgNetFlowLabel =
    new Label():
      styleClass += "title"
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
    avgNetFlowLabel,
    exportedLabel,
    peakExportedLabel,
    importedLabel,
    peakImportedLabel
  )
