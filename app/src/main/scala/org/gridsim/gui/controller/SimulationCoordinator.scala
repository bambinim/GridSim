package org.gridsim.gui.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scalafx.beans.property.ObjectProperty
import org.gridsim.core.simulation.SimulationControllerState.{PAUSED, RUNNING}
import org.gridsim.gui.model.*
import org.gridsim.gui.controller.{SimulationPanel, SimulationSummaryPanel}
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

  val summaryPanel = SimulationSummaryPanel(running.model)
  val entityDetailsPanel = EntityDetailsPanel(running.model, selectedEntity)

  selectedEntity.onChange{
    (_, _, _) => renderCurrent()
  }

  private val panels: Seq[SimulationPanel] =
    Seq(
      summaryPanel,
      entityDetailsPanel
    )

  running.snapshotEvents
    .evalMap(snapshot => IO {
      Platform.runLater {
        val controllerState = running.controller.simulationControllerState
        panels.foreach(_.renderSnapshot(snapshot, controllerState))
      }
    })
    .compile
    .drain
    .unsafeRunAndForget()

  def allPanels: Seq[SimulationPanel] =
    panels

  def renderCurrent(): Unit =
    Platform.runLater {
      val state = running.controller.currentState
      val controllerState = running.controller.simulationControllerState
      panels.foreach(_.renderCurrent(state = state, controller = controllerState))
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


