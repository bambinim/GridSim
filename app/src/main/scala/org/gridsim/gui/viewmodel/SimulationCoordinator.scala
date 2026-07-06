package org.gridsim.gui.viewmodel

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scalafx.beans.property.ObjectProperty
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.{Environment, GridEntityState}
import org.gridsim.core.simulation.SimulationControllerState
import org.gridsim.core.simulation.SimulationControllerState.{PAUSED, RUNNING}
import org.gridsim.gui.model.*
import scalafx.application.Platform
import scalafx.scene.Parent

/**
 * Coordinates GUI-facing simulation updates and commands.
 *
 * @param running the active simulation model and core controller.
 */
class SimulationCoordinator(
  running: RunningSimulation,
  onExit: () => Unit = () => ()
):

  val selectedEntity: ObjectProperty[Selection] = ObjectProperty(
    running.model.grid.nodes
      .find(_.id == "advanced-house-2")
      .map(Selection.SelectedNode.apply)
      .getOrElse(Selection.NoSelection)
  )

  val summaryViewModel = SimulationSummaryViewModel(running.model)
  val entityDetailsViewModel = EntityDetailsViewModel(running.model, selectedEntity)
  val controlViewModel = SimulationControlViewModel(running, onExit)

  selectedEntity.onChange{
    (_, _, _) => renderCurrent()
  }

  running.snapshotEvents
    .evalMap(snapshot => IO {
      Platform.runLater {
        updateWith(snapshot.environment, snapshot.entityStates, snapshot.entityFlows)
      }
    })
    .compile
    .drain
    .unsafeRunAndForget()

  def renderCurrent(): Unit =
    Platform.runLater {
      val state = running.controller.currentState
      updateWith(state.environment, state.entityStates, state.entityFlows)
    }

  private def updateWith(
    environment: Environment,
    entityStates: Map[String, GridEntityState],
    entityFlows: Map[String, Flow[Energy]]
  ): Unit =
    val controllerState = running.controller.simulationControllerState
    summaryViewModel.update(entityFlows, environment, controllerState)
    entityDetailsViewModel.update(entityStates, entityFlows, environment)
    controlViewModel.update(controllerState)

