package org.gridsim.gui.viewmodel

import scalafx.beans.property.{BooleanProperty, StringProperty}
import org.gridsim.core.simulation.SimulationControllerState
import org.gridsim.core.simulation.SimulationControllerState.{PAUSED, RUNNING}
import org.gridsim.gui.model.RunningSimulation

/**
 * ViewModel responsible for managing simulation lifecycle commands and UI state properties.
 * Exposes observable properties to bind with control buttons.
 *
 * @param running the active simulation model and controller wrapper
 * @param onExitCallback a callback to execute when exiting the simulation
 */
class SimulationControlViewModel(
  running: RunningSimulation,
  onExitCallback: () => Unit
):
  private val stoppedProperty = BooleanProperty(false)

  /** Text showing the action for the Play/Pause button ("Play" or "Pause"). */
  val playPauseText: StringProperty = StringProperty("Play")

  /** Text representing the current state of the simulation controller (e.g., "RUNNING", "PAUSED", "STOPPED"). */
  val statusText: StringProperty = StringProperty("PAUSED")

  /** Indicates whether the Play/Pause button should be disabled. */
  val playPauseDisabled: BooleanProperty = BooleanProperty(false)

  /** Indicates whether the Step Once button should be disabled. */
  val stepDisabled: BooleanProperty = BooleanProperty(false)

  /** Indicates whether the Stop button should be disabled. */
  val stopDisabled: BooleanProperty = BooleanProperty(false)

  /** Indicates whether the Exit button should be disabled. */
  val exitDisabled: BooleanProperty = BooleanProperty(false)

  /**
   * Updates the states of the bindable properties based on the simulation controller's state.
   *
   * @param controllerState the current status of the simulation execution
   */
  def update(controllerState: SimulationControllerState): Unit =
    val isStopped = stoppedProperty.value
    playPauseDisabled.value = isStopped
    stopDisabled.value = isStopped
    exitDisabled.value = false

    if (isStopped) then
      stepDisabled.value = true
      playPauseText.value = "Play"
      statusText.value = "STOPPED"
    else
      stepDisabled.value = (controllerState == RUNNING)
      playPauseText.value = if (controllerState == RUNNING) then "Pause" else "Play"
      statusText.value = controllerState.toString

  /**
   * Toggles the simulation status between running and paused.
   */
  def togglePlayPause(): Unit =
    if (!stoppedProperty.value) then
      running.controller.simulationControllerState match
        case RUNNING => running.controller.pause()
        case PAUSED  => running.controller.resume()
    update(running.controller.simulationControllerState)

  /**
   * Advances the simulation by a single tick if currently paused.
   */
  def stepOnce(): Unit =
    if (!stoppedProperty.value && running.controller.simulationControllerState == PAUSED) then
      running.controller.stepOnce()

  /**
   * Stops the simulation and triggers the exit callback.
   */
  def exit(): Unit =
    if (!stoppedProperty.value) then
      running.controller.stop()
      stoppedProperty.value = true
    onExitCallback()
