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

  //TO-DO remove this when implement graph view
  private val graphPlaceholder = new BorderPane:
    style = "-fx-background-color: #111827; -fx-border-color: #374151; -fx-border-width: 1;"
    center = new scalafx.scene.control.Label("Area di Rendering del Grafo (GridGraphView)"):
      style = "-fx-text-fill: #9ca3af; -fx-font-size: 16px;"

  private val summaryView = new SimulationSummaryView(coordinator.summaryViewModel)

  center = new BorderPane:
    top = summaryView
    center = graphPlaceholder
    right = coordinator.entityDetailsPanel.root

  coordinator.renderCurrent()
  coordinator.togglePlayPause()
