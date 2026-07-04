package org.gridsim.gui.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scalafx.beans.property.ObjectProperty
import org.gridsim.core.simulation.SimulationControllerState.{PAUSED, RUNNING}
import org.gridsim.gui.model.*
import scalafx.application.Platform
import scalafx.scene.Parent

/**
 * Coordinates GUI-facing simulation updates and commands.
 *
 * @param running the active simulation model and core controller.
 */
class SimulationCoordinator(running: RunningSimulation):

  val selectedEntity: ObjectProperty[Selection] = ObjectProperty(
    running.model.grid.nodes
      .find(_.id == "advanced-house-2")
      .map(Selection.SelectedNode.apply)
      .getOrElse(Selection.NoSelection)
  )

  val summaryViewModel = SimulationSummaryViewModel(running.model)
  val entityDetailsViewModel = EntityDetailsViewModel(running.model, selectedEntity)

  selectedEntity.onChange{
    (_, _, _) => renderCurrent()
  }

  running.snapshotEvents
    .evalMap(snapshot => IO {
      Platform.runLater {
        val controllerState = running.controller.simulationControllerState
        summaryViewModel.update(snapshot.entityFlows, snapshot.environment, controllerState)
        entityDetailsViewModel.update(snapshot.entityStates, snapshot.entityFlows, snapshot.environment)
      }
    })
    .compile
    .drain
    .unsafeRunAndForget()

  def renderCurrent(): Unit =
    Platform.runLater {
      val state = running.controller.currentState
      val controllerState = running.controller.simulationControllerState
      summaryViewModel.update(state.entityFlows, state.environment, controllerState)
      entityDetailsViewModel.update(state.entityStates, state.entityFlows, state.environment)
    }

  def togglePlayPause(): Unit = {
    running.controller.simulationControllerState match
      case RUNNING => running.controller.pause()
      case PAUSED  => running.controller.resume()
    renderCurrent()
  }

  def stepOnce(): Unit =
    running.controller.stepOnce()

  def stop(): Unit =
    running.controller.stop()
    renderCurrent()
