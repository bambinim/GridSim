package org.gridsim.gui.viewmodel

import org.gridsim.gui.model.{ScenarioPresetId, ScenarioRunConfig}
import org.gridsim.gui.ports.{ScenarioPresetLoader, ScenarioPresetRepository}
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * ViewModel for selecting and starting a simulation scenario.
 *
 * This class coordinates the selection of scenario presets and their loading. It exposes
 * properties for binding with the UI components of the scenario selection view.
 *
 * @tparam A the type of the loaded simulation returned by the loader
 * @param scenarioRepo the repository containing available scenario presets
 * @param loader the loader responsible for loading a selected scenario preset
 */
class ScenarioSelectionViewModel[A](
  scenarioRepo: ScenarioPresetRepository,
  loader: ScenarioPresetLoader[A]
):
  /**
   * List of available scenarios, sorted alphabetically by name.
   * Maps each preset's unique identifier to its human-readable name.
   */
  val scenarios: Seq[(ScenarioPresetId, String)] =
    scenarioRepo.scenarios.toSeq.sortBy(_._2)

  /**
   * Observable buffer containing the sorted names of the available scenarios.
   * Typically bound directly to a list or combo box view.
   */
  val scenariosNames: ObservableBuffer[String] =
    ObservableBuffer.from(scenarios.map(_._2))

  // Observable properties for binding
  
  /** Property holding the name of the currently selected scenario. */
  val selectedScenarioName = StringProperty("No scenario selected")

  /** Property holding detailed hint/description text for the selected scenario. */
  val selectedScenarioHint = StringProperty("The scenario topology is fixed by the project DSL.")

  /** Property bound to the input field specifying the simulation tick duration in minutes. */
  val tickDurationText = StringProperty("15")

  /** Property representing status/feedback messages for the user. */
  val messageText = StringProperty("Select a scenario to continue.")

  /** Property specifying the CSS style class applied to the message text. */
  val messageStyleClass = StringProperty("muted-text")

  /** Property indicating whether the "Start" action should be disabled. */
  val isStartDisabled = BooleanProperty(scenarios.isEmpty)

  private var selectedScenarioId: Option[ScenarioPresetId] = None

  /**
   * Selects a scenario from the list of available presets by index.
   *
   * @param index the index of the scenario in the `scenarios` sequence.
   *              If the index is out of bounds, the selection is cleared.
   */
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

  /**
   * Validates input options and loads the selected scenario.
   *
   * @return `Some(loaded)` if validation succeeds and the scenario is loaded successfully; `None` otherwise.
   */
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
