package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.SimulationCoordinator
import org.gridsim.gui.model.SummaryViewState
import scalafx.scene.Parent
import scalafx.scene.control.{Tab, TabPane}
import scalafx.scene.layout.{BorderPane, VBox}

/**
 * Main view layout for the active simulation screen.
 *
 * This composite view organizes the simulation summary, the grid visualization graph placeholder,
 * the selected entity details panel, and the simulation controls bar.
 *
 * @param coordinator the coordinator that manages state orchestration across the view components
 */
class SimulationView(val coordinator: SimulationCoordinator) extends BorderPane with ViewFX:
  /**
   * The root parent component of this view.
   */
  override def root: Parent = this

  //TO-DO remove this when implement graph view
  private val graphPlaceholder = new BorderPane:
    styleClass += "graph-placeholder"
    center = new scalafx.scene.control.Label("Area di Rendering del Grafo (GridGraphView)"):
      styleClass += "graph-placeholder-text"

  private val summaryView = new SimulationSummaryView(coordinator.summaryViewModel)
  private val entityDetailsView = new EntityDetailsView(coordinator.entityDetailsViewModel)
  private val statisticsView = new StatisticsView(coordinator.statisticsViewModel)
  private val controlView = new SimulationControlView(coordinator.controlViewModel)

  private val rightColumn = new VBox:
    children = Seq(entityDetailsView, statisticsView)

  // TABS alternative to right column
//  private val detailsTabs = new TabPane:
//    tabs = Seq(
//      new Tab:
//        text = "Entity Details"
//        content = entityDetailsView
//        closable = false
//      ,
//      new Tab:
//        text = "Statistics"
//        content = statisticsView
//        closable = false
//    )

  center = new BorderPane:
    top = summaryView
    center = graphPlaceholder
    right = rightColumn

  bottom = controlView

  coordinator.renderCurrent()
