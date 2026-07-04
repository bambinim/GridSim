package org.gridsim.gui.controller

import org.gridsim.gui.model.{ScenarioPresetId, ScenarioRunConfig}
import org.gridsim.gui.ports.{ScenarioPresetLoader, ScenarioPresetRepository}
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class ScenarioSelectionViewModel[A](
  scenarioRepo: ScenarioPresetRepository,
  loader: ScenarioPresetLoader[A]
):
  // Expose scenarios in sorted order
  val scenarios: Seq[(ScenarioPresetId, String)] =
    scenarioRepo.scenarios.toSeq.sortBy(_._2)

  val scenariosNames: ObservableBuffer[String] =
    ObservableBuffer.from(scenarios.map(_._2))

  // Observable properties for binding
  val selectedScenarioName = StringProperty("No scenario selected")
  val selectedScenarioHint = StringProperty("The scenario topology is fixed by the project DSL.")
  val tickDurationText = StringProperty("15")
  val messageText = StringProperty("Select a scenario to continue.")
  val messageStyleClass = StringProperty("muted-text")
  val isStartDisabled = BooleanProperty(scenarios.isEmpty)

  private var selectedScenarioId: Option[ScenarioPresetId] = None

  def selectScenario(index: Int): Unit =
    if index >= 0 && index < scenarios.length then
      val (id, name) = scenarios(index)
      selectedScenarioId = Some(id)
      selectedScenarioName.value = name
      selectedScenarioHint.value = s"Preset id: ${id.value}. Parameters are fixed; only tick duration can be changed here."
      messageText.value = "Ready to start."
      messageStyleClass.value = "muted-text"
    else
      selectedScenarioId = None
      selectedScenarioName.value = "No scenario selected"
      selectedScenarioHint.value = "The scenario topology is fixed by the project DSL."
      messageText.value = "Select a scenario to continue."
      messageStyleClass.value = "muted-text"

  def startScenario(): Option[A] =
    val tickStr = tickDurationText.value.trim
    tickStr.toIntOption match
      case None =>
        updateMessage("Not a valid tick duration inserted", "error-message")
        None
      case Some(value) if value <= 0 =>
        updateMessage("Tick duration must be greater than zero", "error-message")
        None
      case Some(value) =>
        selectedScenarioId match
          case None =>
            updateMessage("No scenario selected", "error-message")
            None
          case Some(id) =>
            loader.load(ScenarioRunConfig(
              presetId = id,
              tickDurationMinutes = value.minutes
            )) match
              case Right(loaded) =>
                updateMessage("Scenario loaded.", "success-message")
                Some(loaded)
              case Left(err) =>
                updateMessage(err, "error-message")
                None

  private def updateMessage(msg: String, style: String): Unit =
    messageText.value = msg
    messageStyleClass.value = style
