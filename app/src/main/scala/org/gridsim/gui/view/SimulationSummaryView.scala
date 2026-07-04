package org.gridsim.gui.view

import org.gridsim.gui.controller.SimulationSummaryViewModel
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class SimulationSummaryView(viewModel: SimulationSummaryViewModel) extends VBox with ViewFX:
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
