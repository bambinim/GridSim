package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.{SimulationCoordinator, SimulationViewLayout}
import scalafx.scene.Parent
import scalafx.scene.control.{Label, SplitPane, Tab, TabPane}
import scalafx.scene.layout.{BorderPane, Priority, StackPane, VBox}

import org.gridsim.gui.view.layouts.{
  SimulationLayoutStrategy,
  SplitSimulationLayout,
  TabsSimulationLayout
}
import scalafx.application.Platform

/** Main view layout for the active simulation screen.
  *
  * This composite view organizes the scenario title, grid visualization,
  * selected entity details, statistics, and simulation controls.
  *
  * @param coordinator
  *   the coordinator that manages state orchestration across the view
  *   components
  */
class SimulationView(val coordinator: SimulationCoordinator)
    extends BorderPane
    with ViewFX:
  override def root: Parent = this

  private val scenarioTitle = new Label(coordinator.scenarioName):
    styleClass ++= Seq("title", "simulation-title", "main-title")

  private val controlView = new SimulationControlView(
    coordinator.controlViewModel
  )

  private val graphView = GraphView(coordinator)
  private val statsView = StatisticsView(coordinator)

  private val stack = new StackPane

  private val centerContent = new VBox:
    children = Seq(scenarioTitle, stack, controlView)

  VBox.setVgrow(stack, Priority.Always)
  center = centerContent

  def updateLayout(layout: SimulationViewLayout): Unit =
    val strategy: SimulationLayoutStrategy = layout match
      case SimulationViewLayout.Tabs  => new TabsSimulationLayout()
      case SimulationViewLayout.Split => new SplitSimulationLayout()

    stack.children = Seq(
      strategy.build(graphView.graphArea, statsView.statisticsArea)
    )

  coordinator.controlViewModel.detailsLayout.onChange { (_, _, layout) =>
    updateLayout(layout)
  }
