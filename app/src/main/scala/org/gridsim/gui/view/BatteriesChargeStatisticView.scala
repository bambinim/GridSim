package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.BatteriesChargeStatisticViewModel
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

/**
 * View panel showing aggregate battery charge across the fleet.
 *
 * @param viewModel the viewmodel driving this panel
 */
class BatteriesChargeStatisticView(viewModel: BatteriesChargeStatisticViewModel) extends VBox with ViewFX:
  override def root: Parent = this

  styleClass += "statistics"

  private val averageLabel =
    new Label():
      styleClass += "bold"
      styleClass += "statistic"
      text <== viewModel.averageText

  private val minLabel =
    new Label():
      styleClass += "statistic"
      text <== viewModel.totalText

  private val maxLabel =
    new Label():
      styleClass += "statistic"
      text <== viewModel.maxText

  children = Seq(averageLabel, minLabel, maxLabel)
