package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.{SimulationCoordinator, DetailsLayout}
import scalafx.geometry.Pos
import scalafx.scene.Parent
import scalafx.scene.control.{Label, ScrollPane, Separator, SplitPane, Tab, TabPane}
import scalafx.scene.layout.{BorderPane, HBox, Priority, StackPane, VBox}

/**
 * Main view layout for the active simulation screen.
 *
 * This composite view organizes the scenario title, grid visualization,
 * selected entity details, statistics, and simulation controls.
 *
 * @param coordinator
 *   the coordinator that manages state orchestration across the view
 *   components
 */
class SimulationView(val coordinator: SimulationCoordinator) extends BorderPane with ViewFX:
  override def root: Parent = this

  private val scenarioTitle = new Label(coordinator.scenarioName):
    styleClass ++= Seq("title", "simulation-title", "main-title")

  private val controlView = new SimulationControlView(coordinator.controlViewModel)

  private val graphTab = GraphView(coordinator)
  private val statsTab = StatisticsView(coordinator)

  private val graphSplit = GraphView(coordinator)
  private val statsSplit = StatisticsView(coordinator)

  private val tabLayout = new TabPane:
    tabs = Seq(
      new Tab:
        text = "Graph"
        content = graphTab.graphArea
        closable = false
      ,
      new Tab:
        text = "Statistics"
        content = statsTab.statisticsArea
        closable = false
    )

  private val splitLayout = new SplitPane:
    styleClass += "sym-split"
    orientation = scalafx.geometry.Orientation.Vertical
    items ++= Seq(
      graphSplit.graphArea,
      statsSplit.statisticsArea
    )

  private val stack = new StackPane:
    children = Seq(splitLayout, tabLayout)

  private val centerContent = new VBox:
    children = Seq(scenarioTitle, stack, controlView)

  VBox.setVgrow(stack, Priority.Always)

  center = centerContent

  coordinator.controlViewModel.detailsLayout.onChange { (_, _, layout) =>
    tabLayout.visible = layout == DetailsLayout.Tabs
    tabLayout.managed = layout == DetailsLayout.Tabs

    splitLayout.visible = layout == DetailsLayout.Split
    splitLayout.managed = layout == DetailsLayout.Split
  }

  coordinator.renderCurrent()
