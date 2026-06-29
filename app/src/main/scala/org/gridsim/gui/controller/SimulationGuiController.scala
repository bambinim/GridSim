package org.gridsim.gui.controller

import org.gridsim.core.simulation.{SimulationController, SimulationControllerState, SimulationState, SimulationSnapshot}
import scalafx.application.Platform

/**
 * Controller for the simulation GUI view, managing the interaction with the core simulation runner.
 * It respects the Single Responsibility Principle by hiding core simulation controller implementation
 * details from the view.
 *
 * @param coreController the core simulation runner to delegate simulation tasks to.
 */
class SimulationGuiController(private val coreController: SimulationController):

  private var onChangedCallback: Option[SimulationSnapshot => Unit] = None

  coreController.addStateListener { snapshot =>
    Platform.runLater {
      onChangedCallback.foreach(_(snapshot))
    }
  }

  def setOnChanged(callback: SimulationSnapshot => Unit): Unit =
    onChangedCallback = Some(callback)

  def togglePlayPause(): Unit =
    if coreController.simulationControllerState == SimulationControllerState.RUNNING then
      coreController.pause()
    else
      coreController.start()

  /**
   * Returns the current state of the simulation.
   */
  def currentState: SimulationState =
    coreController.currentState

  /**
   * Returns whether the simulation is currently running.
   */
  def isRunning: Boolean =
    coreController.simulationControllerState == SimulationControllerState.RUNNING

  /**
   * Returns the current lifecycle state of the simulation controller.
   */
  def controllerState: SimulationControllerState =
    coreController.simulationControllerState

  /**
   * Advances the simulation by a single tick.
   */
  def stepOnce(): Unit =
    coreController.stepOnce()

  /**
   * Stops the simulation and cleans up resources.
   */
  def stop(): Unit =
    coreController.stop()
