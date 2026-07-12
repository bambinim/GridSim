package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.CableOverloadStatisticViewModel
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

/**
 * View panel showing how often cables exceeded their rated capacity.
 *
 * @param viewModel the viewmodel driving this panel
 */
class CableOverloadStatisticView(viewModel: CableOverloadStatisticViewModel) extends VBox with ViewFX:
  override def root: Parent = this

  styleClass += "statistics"

  private val overloadedLabel =
    new Label():
      text <== viewModel.overloadedText

  children = Seq(overloadedLabel)
