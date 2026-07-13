package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.SimulationCoordinator
import scalafx.geometry.Pos
import scalafx.scene.Parent
import scalafx.scene.control.{Label, ScrollPane, Separator, SplitPane}
import scalafx.scene.layout.{HBox, VBox}

class StatisticsView(val coordinator: SimulationCoordinator):

  private val flowStatView = new FlowStatisticView(coordinator.flowStatisticViewModel)
  private val batteryChargeStatView = new BatteriesChargeStatisticView(coordinator.batteryChargeStatisticViewModel)
  private val cableOverloadStatView = new CableOverloadStatisticView(coordinator.cableOverloadStatisticViewModel)
  private val simulationTimeStatView = new SimulationTimeStatisticView(coordinator.simulationTimeStatisticViewModel)
  private val netFlowChartView = new NetFlowChartStatisticView(coordinator.netFlowChartStatisticViewModel)
  
  private def statSection(title: String, content: Parent): VBox =
    val titleLabel = new Label(title) {
      styleClass += "entity-title-main"
    }
    val header = new HBox(8) {
      alignment = Pos.CenterLeft
      children = Seq(titleLabel)
    }
    val separator = new Separator {
      styleClass += "details-separator"
    }
    new VBox(10) {
      styleClass += "entity-card-main"
      children = Seq(header, separator, content)
    }

  private val statisticsStack = new VBox(12) {
    styleClass += "entity-details-view"
    children = Seq(
      statSection("Simulation Time", simulationTimeStatView),
      statSection("Flow", flowStatView),
      statSection("Battery Charge", batteryChargeStatView),
      statSection("Cable Overload", cableOverloadStatView)
    )
  }

  private val statisticsScroll = new ScrollPane:
    fitToWidth = true
    minWidth = 220
    content = statisticsStack
    styleClass += "entity-details-scroll-pane"

  val statisticsArea: SplitPane = new SplitPane:
    items ++= Seq(netFlowChartView, statisticsScroll)
    dividerPositions = 0.75
