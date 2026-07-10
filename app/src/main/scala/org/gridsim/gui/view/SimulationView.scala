package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.SimulationCoordinator
import org.gridsim.gui.model.SummaryViewState
import scalafx.geometry.{Insets, Orientation}
import scalafx.scene.Parent
import scalafx.scene.control.{SplitPane, Tab, TabPane}
import scalafx.scene.layout.{BorderPane, Priority, VBox}

/**
 * Main view layout for the active simulation screen.
 *
 * This composite view organizes the simulation summary, the grid visualization graph placeholder,
 * the selected entity details panel, and the simulation controls bar.
 *
 * @param coordinator the coordinator that manages state orchestration across the view components
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
  private val statisticsView = new FlowStatisticView(coordinator.flowStatisticViewModel)
  private val netFlowChartView = new NetFlowChartStatisticView(coordinator.netFlowChartStatisticViewModel)
  private val controlView = new SimulationControlView(coordinator.controlViewModel)

  private val graphArea = new BorderPane:
    center = graphPlaceholder
    right = entityDetailsView

  private val statisticsArea = new BorderPane:
    center = netFlowChartView
    right = statisticsView

  private val detailsTabs = new TabPane:
    tabs = Seq(
      new Tab:
        text = "Graph"
        content = graphArea
        closable = false
      ,
      new Tab:
        text = "Statistics"
        content = statisticsArea
        closable = false
    )

  VBox.setVgrow(detailsTabs, Priority.Always)

  center = new VBox:
    children = Seq(summaryView, detailsTabs, controlView)

  coordinator.renderCurrent()
