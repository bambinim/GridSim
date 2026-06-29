package org.gridsim.gui.controller

import org.gridsim.core.simulation.*
import org.gridsim.core.simulation.SimulationControllerState.{PAUSED, RUNNING}
import org.gridsim.gui.model.*
import scalafx.application.Platform

/**
 * Controller for the simulation GUI view, managing the interaction with the core simulation runner.
 * It respects the Single Responsibility Principle by hiding core simulation controller implementation
 * details from the view.
 *
 * @param running the active simulation model and core controller.
 */
class SimulationGuiController(running: RunningSimulation):

  private var selectedEntityId: Option[String] = None
  private var onChanged: SimulationDashboardState => Unit = _ => ()

  private var dashboard: SimulationDashboardState =
    dashboardFromCurrentState()

  running.controller.addStateListener { snapshot =>
    publish(SimulationDashboardMapper.toDashboard(running.model, snapshot, selectedEntityId))
  }

  def currentDashboard: SimulationDashboardState =
    dashboard

  def setOnChanged(callback: SimulationDashboardState => Unit): Unit =
    onChanged = callback
    callback(dashboard)

  def togglePlayPause(): Unit =
    running.controller.simulationControllerState match
      case RUNNING => running.controller.pause()
      case PAUSED => running.controller.resume()

  def stepOnce(): Unit =
    running.controller.stepOnce()

  def stop(): Unit =
    running.controller.stop()

  def selectEntity(entityId: String): Unit =
    selectedEntityId = Some(entityId)
    publish(dashboardFromCurrentState())

  def clearSelection(): Unit =
    selectedEntityId = None
    publish(dashboardFromCurrentState())

  private def publish(next: SimulationDashboardState): Unit =
    Platform.runLater {
      dashboard = next
      onChanged(next)
    }

  private def dashboardFromCurrentState(): SimulationDashboardState =
    SimulationDashboardMapper.toDashboard(
      running.model,
      SimulationSnapshot(
        running.controller.currentState,
        running.controller.simulationControllerState
      ),
      selectedEntityId
    )
