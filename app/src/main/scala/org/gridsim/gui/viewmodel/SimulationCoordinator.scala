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
 * This coordinator subscribes to simulation snapshot events, runs background updates
 * in the JavaFX Application Thread via `Platform.runLater`, and coordinates interactions
 * between the various sub-ViewModels.
 *
 * @param running the active simulation model and core controller wrapper
 * @param onExit callback invoked when the simulation is stopped and exited
 */
class SimulationCoordinator(
  running: RunningSimulation,
  onExit: () => Unit = () => ()
):

  /** Property tracking the currently selected entity in the GUI. */
  val selectedEntity: ObjectProperty[Selection] = ObjectProperty(
    Selection.NoSelection
  )

  /** ViewModel managing the overall simulation status and metric summary. */
  val summaryViewModel = SimulationSummaryViewModel(running.model)

  /** ViewModel managing the detailed properties and components of the selected entity. */
  val entityDetailsViewModel = EntityDetailsViewModel(running.model, selectedEntity)

  /** ViewModel controlling simulation commands like play, pause, step, and stop. */
  val controlViewModel = SimulationControlViewModel(running, onExit)

  selectedEntity.onChange{
    (_, _, _) => renderCurrent()
  }

  running.snapshotSignal.discrete
    .evalMap(snapshot => IO {
      Platform.runLater {
        updateWith(snapshot.environment, snapshot.entityStates, snapshot.entityFlows)
      }
    })
    .compile
    .drain
    .unsafeRunAndForget()

  /**
   * Forces a render update of selected entity details using the current state stored inside the simulation controller.
   */
  def renderCurrent(): Unit =
    Platform.runLater {
      val state = running.controller.currentState
      entityDetailsViewModel.update(state.entityStates, state.entityFlows, state.environment)
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

