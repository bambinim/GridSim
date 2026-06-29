package org.gridsim.gui.view

import org.gridsim.gui.controller.SimulationGuiController
import org.gridsim.gui.model.SimulationDashboardState
import org.gridsim.gui.view.{SimulationSummaryView, SimulationToolBar}
import scalafx.scene.Parent
import scalafx.scene.control.Label
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.control.Button

/**
 * Main view for the active simulation dashboard.
 * It observes state changes through the SimulationGuiController.
 */
class SimulationView(val controller: SimulationGuiController) extends BorderPane with ViewFX:
  override def root: Parent = this

  private val toolbar = new SimulationToolBar(
    onTogglePlayPause = () => controller.togglePlayPause(),
    onStep = () => controller.stepOnce(),
    onStop = () => controller.stop()
  )

  private val summaryView = new SimulationSummaryView

  top = toolbar
  center = new VBox(16):
    children = Seq(
      summaryView
    )


  render(controller.currentDashboard)
  
  controller.setOnChanged(render)
  
  private def render(state: SimulationDashboardState): Unit =
    toolbar.render(state)
    summaryView.render(state)