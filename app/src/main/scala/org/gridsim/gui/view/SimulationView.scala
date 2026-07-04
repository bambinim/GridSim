package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.SimulationCoordinator
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
    styleClass += "graph-placeholder"
    center = new scalafx.scene.control.Label("Area di Rendering del Grafo (GridGraphView)"):
      styleClass += "graph-placeholder-text"

  private val summaryView = new SimulationSummaryView(coordinator.summaryViewModel)
  private val entityDetailsView = new EntityDetailsView(coordinator.entityDetailsViewModel)

  center = new BorderPane:
    top = summaryView
    center = graphPlaceholder
    right = entityDetailsView

  coordinator.renderCurrent()
  coordinator.togglePlayPause()
