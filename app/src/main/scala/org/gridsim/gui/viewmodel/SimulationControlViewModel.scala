package org.gridsim.gui.viewmodel

import scalafx.beans.property.{BooleanProperty, StringProperty}
import org.gridsim.core.simulation.SimulationControllerState
import org.gridsim.core.simulation.SimulationControllerState.{PAUSED, RUNNING}
import org.gridsim.gui.model.RunningSimulation

/**
 * ViewModel responsible for managing simulation lifecycle commands and UI state properties.
 * Exposes observable properties to bind with control buttons.
 */
class SimulationControlViewModel(
  running: RunningSimulation,
  onExitCallback: () => Unit
):
  private val stoppedProperty = BooleanProperty(false)

  // Bindable properties for the View
  val playPauseText: StringProperty = StringProperty("Play")
  val statusText: StringProperty = StringProperty("PAUSED")

  val playPauseDisabled: BooleanProperty = BooleanProperty(false)
  val stepDisabled: BooleanProperty = BooleanProperty(false)
  val stopDisabled: BooleanProperty = BooleanProperty(false)
  val exitDisabled: BooleanProperty = BooleanProperty(false)

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

  def togglePlayPause(): Unit =
    if (!stoppedProperty.value) then
      running.controller.simulationControllerState match
        case RUNNING => running.controller.pause()
        case PAUSED  => running.controller.resume()
    update(running.controller.simulationControllerState)

  def stepOnce(): Unit =
    if (!stoppedProperty.value && running.controller.simulationControllerState == PAUSED) then
      running.controller.stepOnce()

  def exit(): Unit =
    if (!stoppedProperty.value) then
      running.controller.stop()
      stoppedProperty.value = true
    onExitCallback()
