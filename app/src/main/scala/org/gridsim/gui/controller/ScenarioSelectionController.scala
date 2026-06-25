package org.gridsim.gui.controller

import org.gridsim.gui.model.{ScenarioPresetId, ScenarioRunConfig}
import org.gridsim.gui.ports.{ScenarioPresetLoader, ScenarioPresetRepository}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

case class ScenarioSelectionState(
  selectedScenarioId: Option[ScenarioPresetId] = None,
  tickDuration: FiniteDuration = 15.minutes
)

class ScenarioSelectionController[A](
  scenarioRepo: ScenarioPresetRepository,
  loader: ScenarioPresetLoader[A]
):
  def initialScenario: ScenarioSelectionState =
    ScenarioSelectionState()

  def availableScenarios: Map[ScenarioPresetId, String] =
    scenarioRepo.scenarios

  def selectScenario(
    state: ScenarioSelectionState,
    id: ScenarioPresetId
  ): Either[String, ScenarioSelectionState] =
    if availableScenarios.contains(id) then
      Right(state.copy(selectedScenarioId = Some(id)))
    else
      Left(s"Unknown scenario: ${id.value}")

  def updateTickDuration(
    state: ScenarioSelectionState,
    tick: FiniteDuration
  ): Either[String, ScenarioSelectionState] =
    if tick.length > 0 then
      Right(state.copy(tickDuration = tick))
    else
      Left(s"Tick duration must be greater than zero")

  def startSelectedScenario(state: ScenarioSelectionState): Either[String, A] =
    state.selectedScenarioId match
      case Some(id) =>
        loader.load(ScenarioRunConfig(
          presetId = id,
          tickDurationMinutes = state.tickDuration
        ))
      case None =>
        Left("No scenario selected")

