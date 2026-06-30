package org.gridsim.gui.view

import org.gridsim.gui.controller.SimulationCoordinator
import org.gridsim.gui.model.SummaryViewState
import scalafx.scene.Parent
import scalafx.scene.layout.{BorderPane, VBox}

/**
 * Main view for the active simulation summary.
 */
class SimulationView(val coordinator: SimulationCoordinator) extends BorderPane with ViewFX:
  override def root: Parent = this

  center = new BorderPane:
    top = coordinator.summaryPanel.root


  coordinator.renderCurrent()
