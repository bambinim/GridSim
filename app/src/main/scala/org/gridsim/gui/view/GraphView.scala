package org.gridsim.gui.view

import org.gridsim.gui.viewmodel.SimulationCoordinator
import scalafx.scene.control.SplitPane
import scalafx.scene.layout.BorderPane

class GraphView(coordinator: SimulationCoordinator):

  private val gridGraphView = new GridGraphView(coordinator.graphViewModel)
  private val entityDetailsView = new EntityDetailsView(coordinator.entityDetailsViewModel)
  
  val graphArea: SplitPane = new SplitPane:
    items ++= Seq(gridGraphView, entityDetailsView)
    dividerPositions = 0.75
