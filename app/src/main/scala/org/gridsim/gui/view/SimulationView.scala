package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.SimulationCoordinator
import scalafx.scene.Parent
import scalafx.scene.control.{Label, Tab, TabPane}
import scalafx.scene.layout.{BorderPane, Priority, VBox}

/**
 * Main view layout for the active simulation screen.
 *
 * This composite view organizes the simulation summary, the grid visualization
 * graph placeholder, the selected entity details panel, and the simulation
 * controls bar.
 *
 * @param coordinator
 *   the coordinator that manages state orchestration across the view
 *   components
 */
class SimulationView(val coordinator: SimulationCoordinator)
    extends BorderPane
    with ViewFX:
  override def root: Parent = this

  private val summaryView = new SimulationSummaryView(coordinator.scenarioName, coordinator.summaryViewModel)
  private val entityDetailsView = new EntityDetailsView(coordinator.entityDetailsViewModel)
  private val flowStatView = new FlowStatisticView(coordinator.flowStatisticViewModel)
  private val batteryChargeStatView = new BatteriesChargeStatisticView(coordinator.batteryChargeStatisticViewModel)
  private val cableOverloadStatView = new CableOverloadStatisticView(coordinator.cableOverloadStatisticViewModel)
  private val simulationTimeStatView = new SimulationTimeStatisticView(coordinator.simulationTimeStatisticViewModel)
  private val netFlowChartView = new NetFlowChartStatisticView(coordinator.netFlowChartStatisticViewModel)
  private val controlView = new SimulationControlView(coordinator.controlViewModel)
  private val gridGraphView = new GridGraphView(coordinator.graphViewModel)

  private val graphArea = new BorderPane:
    center = gridGraphView
    right = entityDetailsView

  private val statisticTabs = new TabPane:
    tabs = Seq(
      new Tab:
        text = "Flow"
        content = flowStatView
        closable = false
      ,
      new Tab:
        text = "Battery Charge"
        content = batteryChargeStatView
        closable = false
      ,
      new Tab:
        text = "Cable Overload"
        content = cableOverloadStatView
        closable = false
      ,
      new Tab:
        text = "Simulation Time"
        content = simulationTimeStatView
        closable = false
    )

  private val statisticsArea = new BorderPane:
    center = netFlowChartView
    right = statisticTabs

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
