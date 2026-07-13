package org.gridsim.gui.viewmodel

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scalafx.beans.property.ObjectProperty
import org.gridsim.core.common.{Energy, Flow}
import org.gridsim.core.model.{Environment, GridEntityState}
import org.gridsim.core.model.network.Cable
import org.gridsim.gui.model.*
import org.gridsim.statistics.{StatKey, StatisticsRegistry}
import scalafx.application.Platform
import scala.concurrent.duration.FiniteDuration

/**
 * Coordinates GUI-facing simulation updates and commands.
 *
 * This coordinator subscribes to simulation snapshot events, runs background
 * updates in the JavaFX Application Thread via `Platform.runLater`, and
 * coordinates interactions between the various sub-ViewModels.
 *
 * @param running
 *   the active simulation model and core controller wrapper
 * @param onExit
 *   callback invoked when the simulation is stopped and exited
 */
class SimulationCoordinator(
    running: RunningSimulation,
    onExit: () => Unit = () => ()
):

  val scenarioName: String = running.name

  /** Property tracking the currently selected entity in the GUI. */
  val selectedEntity: ObjectProperty[Selection] = ObjectProperty(Selection.NoSelection)

  /** ViewModel managing the detailed properties and components of the selected entity. */
  val entityDetailsViewModel = EntityDetailsViewModel(running.model, selectedEntity, () => running.controller.configuration.delta)

  /** ViewModel controlling simulation commands like play, pause, step, and stop. */
  val controlViewModel = SimulationControlViewModel(running, onExit, () => renderCurrent())

  /** ViewModels managing the statistics. */
  val flowStatisticViewModel = FlowStatisticViewModel()
  val netFlowChartStatisticViewModel = NetFlowChartStatisticViewModel()
  val batteryChargeStatisticViewModel = BatteriesChargeStatisticViewModel()
  val cableOverloadStatisticViewModel = CableOverloadStatisticViewModel()
  val simulationTimeStatisticViewModel = SimulationTimeStatisticViewModel()

  running.statisticsSignal.discrete
    .map(StatisticsRegistry.engine.extract)
    .evalMap(board => IO {
      Platform.runLater {
        flowStatisticViewModel.update(board.get(StatKey.FlowStatKey))
        netFlowChartStatisticViewModel.update(board.get(StatKey.NetFlowHistoryStatKey))
        batteryChargeStatisticViewModel.update(board.get(StatKey.BatteryChargeStatKey))
        cableOverloadStatisticViewModel.update(board.get(StatKey.CableOverloadStatKey))
        simulationTimeStatisticViewModel.update(board.get(StatKey.SimTimeStatKey))
      }
    }).compile.drain.unsafeRunAndForget()

  selectedEntity.onChange{
    (_, _, _) => renderCurrent()
  }

  /** ViewModel managing the graph visualization. */
  val graphViewModel = GridGraphViewModel(running.controller.configuration.delta, running.model.grid, selectedEntity)

  selectedEntity.onChange { (_, _, _) =>
    renderCurrent()
  }

  running.snapshotSignal.discrete
    .evalMap(snapshot =>
      IO {
        Platform.runLater {
          updateWith(
            snapshot.environment,
            snapshot.entityStates,
            snapshot.entityFlows,
            snapshot.cableLoads
          )
        }
      }
    ).compile.drain.unsafeRunAndForget()

  /** Forces a render update of selected entity details using the current state stored inside the simulation controller. */
  def renderCurrent(): Unit =
    Platform.runLater {
      val state = running.controller.currentState

      entityDetailsViewModel.update(
        state.entityStates,
        state.entityFlows,
        state.cableLoads,
        state.environment
      )
    }

  private def updateWith(
      environment: Environment,
      entityStates: Map[String, GridEntityState],
      entityFlows: Map[String, Flow[Energy]],
      cableLoads: Map[Cable, Energy]
  ): Unit =
    val controllerState = running.controller.simulationControllerState
    entityDetailsViewModel.update(entityStates, entityFlows, cableLoads, environment)
    controlViewModel.update(controllerState)
    graphViewModel.update(entityFlows, cableLoads)
